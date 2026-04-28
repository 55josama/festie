package com.ojosama.eventservice.event.domain.support;

public record FieldChange(
        String fieldName,
        Object before,
        Object after
) {
    public boolean isChanged() {
        if (before == null && after == null) {
            return false;
        }

        if (before == null || after == null) {
            return true;
        }
        return !before.equals(after);
    }

    @Override
    public String toString() {
        return String.format("{\"fieldName\":\"%s\",\"before\":%s,\"after\":%s}",
                fieldName, formatValue(before), formatValue(after));
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            String escaped = ((String) value)
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
            return "\"" + escaped + "\"";
        }
        return value.toString();
    }
}

