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

package com.example.repository;

import com.example.domain.Genre;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.repository.PageableRepository;

import jakarta.validation.constraints.NotBlank;

import static io.micronaut.data.model.query.builder.sql.Dialect.MYSQL;

@JdbcRepository(dialect = MYSQL) // <1>
public interface GenreRepository extends PageableRepository<Genre, Long> { // <2>

    Genre save(@NonNull @NotBlank String name); // <3>

    long update(@Id long id, @NonNull @NotBlank String name);
}
