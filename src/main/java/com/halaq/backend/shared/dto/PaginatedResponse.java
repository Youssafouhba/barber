package com.halaq.backend.shared.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {
    private List<T> items;
    private int page;
    private int pageSize;
    private int total;
    private int totalPages;
    private boolean hasNextPage;
    private boolean hasPreviousPage;

    // Getters, setters, and builder pattern
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private final PaginatedResponse<T> response = new PaginatedResponse<>();

        public Builder<T> items(List<T> items) {
            response.items = items;
            return this;
        }

        public Builder<T> page(int page) {
            response.page = page;
            return this;
        }

        public Builder<T> pageSize(int pageSize) {
            response.pageSize = pageSize;
            return this;
        }

        public Builder<T> total(int total) {
            response.total = total;
            return this;
        }

        public Builder<T> totalPages(int totalPages) {
            response.totalPages = totalPages;
            return this;
        }

        public Builder<T> hasNextPage(boolean hasNextPage) {
            response.hasNextPage = hasNextPage;
            return this;
        }

        public Builder<T> hasPreviousPage(boolean hasPreviousPage) {
            response.hasPreviousPage = hasPreviousPage;
            return this;
        }

        public PaginatedResponse<T> build() {
            return response;
        }
    }
}