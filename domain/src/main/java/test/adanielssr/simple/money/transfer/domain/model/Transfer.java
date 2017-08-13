package test.adanielssr.simple.money.transfer.domain.model;

import lombok.Data;

/**
 * Created by arodrigues on 13/08/2017.
 */

@Data
public class Transfer {

    private Long accountNumberFrom;

    private Long accountNumberTo;

    private Double amount;

    private TransferType type;
}
