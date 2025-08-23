package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class OrderPriorityConverter implements AttributeConverter<Order.OrderPriority, String> {
    
    @Override
    public String convertToDatabaseColumn(Order.OrderPriority priority) {
        if (priority == null) {
            return "SCHEDULED"; // Default for new records
        }
        return priority.name();
    }
    
    @Override
    public Order.OrderPriority convertToEntityAttribute(String dbValue) {
        // Handle null or empty strings
        if (dbValue == null || dbValue.trim().isEmpty()) {
            return Order.OrderPriority.SCHEDULED;
        }
        
        // Handle valid enum values
        try {
            return Order.OrderPriority.valueOf(dbValue.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handle invalid enum values gracefully
            System.out.println("Warning: Invalid OrderPriority value '" + dbValue + "' found in database. Defaulting to SCHEDULED.");
            return Order.OrderPriority.SCHEDULED;
        }
    }
}
