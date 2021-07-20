package bookdelivery.external;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

//@Entity
//@Table(name="Payment_table")
public class Payment {

    /*
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    //private Long id;
    private Long paymentid;
    private Long settlementid;
    private Long orderid;
    private Long itemid;
    private Integer itemPrice;
    private Integer qty;
    private String orderStatus;

    @PostPersist
    public void onPostPersist(){
        Paid paid = new Paid();
        BeanUtils.copyProperties(this, paid);
        paid.publishAfterCommit();

    }
*/
    /*
    public Long getId() {
        return id;
    }

    
    public void setId(Long id) {
        this.id = id;
    }
    */

    private Long paymentid;
    private Long settlementid;
    private Long orderid;
    private Long itemid;
    private Integer itemPrice;
    private Integer qty;
    private String orderStatus;
    
    public Long getPaymentid() {
        return paymentid;
    }

    public void setPaymentid(Long paymentid) {
        this.paymentid = paymentid;
    }
    public Long getSettlementid() {
        return settlementid;
    }

    public void setSettlementid(Long settlementid) {
        this.settlementid = settlementid;
    }
    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }
    public Long getItemid() {
        return itemid;
    }

    public void setItemid(Long itemid) {
        this.itemid = itemid;
    }
    public Integer getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(Integer itemPrice) {
        this.itemPrice = itemPrice;
    }
    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }




}
