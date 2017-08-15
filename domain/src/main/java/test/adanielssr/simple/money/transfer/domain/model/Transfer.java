package test.adanielssr.simple.money.transfer.domain.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by arodrigues on 13/08/2017.
 */

@Data
@EqualsAndHashCode
public class Transfer {

    private Long transferNumber;

    private Long accountNumberFrom;

    private Long accountNumberTo;

    private Double amount;

    private Date transferTimestamp;

    private TransferStatus status;
}
