package testforqfa.domain;

import java.util.*;
import lombok.*;
import testforqfa.domain.*;
import testforqfa.infra.AbstractEvent;

@Data
@ToString
public class OrderPlaced extends AbstractEvent {

    private Long id;
    private Long productId;
    private String productName;
    private Integer qty;
}
