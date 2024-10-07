package testforqfa.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import testforqfa.domain.*;
import testforqfa.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
@ToString
public class DeliveryStarted extends AbstractEvent {

    private Long id;
    private Integer stock;

    public DeliveryStarted(Inventory aggregate) {
        super(aggregate);
    }

    public DeliveryStarted() {
        super();
    }
}
//>>> DDD / Domain Event
