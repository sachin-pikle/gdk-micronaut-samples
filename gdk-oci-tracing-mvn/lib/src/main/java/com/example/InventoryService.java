/*
 * Copyright 2025 Oracle and/or its affiliates
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import io.micronaut.tracing.annotation.NewSpan;
import io.micronaut.tracing.annotation.SpanTag;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class InventoryService {

    private static final String storeName = "my_store";

    private final Tracer tracer;
    private final WarehouseClient warehouse;
    private final Map<String, Integer> inventory = new ConcurrentHashMap<>();

    InventoryService(Tracer tracer, WarehouseClient warehouse) { // <1>
        this.tracer = tracer;
        this.warehouse = warehouse;

        inventory.put("laptop", 4);
        inventory.put("desktop", 2);
        inventory.put("monitor", 11);
    }

    public Collection<String> getProductNames() {
        return inventory.keySet();
    }

    @NewSpan("stock-counts") // <2>
    public Map<String, Integer> getStockCounts(@SpanTag("inventory.item") String item) { // <3>
        Map<String, Integer> counts = new HashMap<>();
        if (inventory.containsKey(item)) {
            int count = inventory.get(item);
            counts.put("store", count);

            if (count < 10) {
                counts.put("warehouse", inWarehouse(storeName, item));
            }
        }

        return counts;
    }

    private int inWarehouse(String store, String item) {
        Span.current().setAttribute("inventory.store-name", store); // <4>

        return warehouse.getItemCount(store, getUPC(item));
    }

    public void order(String item, int count) {
        orderFromWarehouse(item, count);
        if (inventory.containsKey(item)) {
            count += inventory.get(item);
        }
        inventory.put(item, count);
    }

    private void orderFromWarehouse(String item, int count) {
        Span span = tracer.spanBuilder("warehouse-order") // <5>
                .setAttribute("item", item)
                .setAttribute("count", count)
                .startSpan();

        warehouse.order(Map.of(
                "store", storeName,
                "product", item,
                "amount", count,
                "upc", getUPC(item)));

        span.end(); // <6>
    }

    private int getUPC(String item) {
        return Math.abs(item.hashCode());
    }
}
