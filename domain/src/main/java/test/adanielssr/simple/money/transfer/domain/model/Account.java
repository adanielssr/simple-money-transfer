package test.adanielssr.simple.money.transfer.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by arodrigues on 13/08/2017.
 */

@Data
@EqualsAndHashCode
public class Account {

    private Long accountNumber;

    private Double balance;
}