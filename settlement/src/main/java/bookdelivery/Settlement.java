package bookdelivery;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Settlement_table")
public class Settlement {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long settlementid; 
    private Long orderId;
    private Long sellerid;
    private Long itemid;
    private Integer qty;
    private Integer itemPrice;
    private String orderStatus;

    @PostPersist    
    public void onPostPersist(){
        
        /*
        OrderFinished orderFinished = new OrderFinished();
        BeanUtils.copyProperties(this, orderFinished);
        orderFinished.publishAfterCommit();
        */
        Settled settled = new Settled();
        BeanUtils.copyProperties(this, settled);
        settled.publishAfterCommit();
    }


    @PostUpdate
    public void onPostUpdate(){

        Settled settled = new Settled();
        BeanUtils.copyProperties(this, settled);
        settled.publishAfterCommit();

        bookdelivery.external.Payment payment = new bookdelivery.external.Payment();
        //mappings goes here
        payment.setSettlementid(settled.getSettlementid());
        payment.setOrderid(settled.getOrderId());
        //payment.setCustomerName(settled.getCustomerName());
        //payment.setItemName(settled.getItemName());
        payment.setQty(settled.getQty());
        payment.setItemid(settled.getItemid());
        payment.setItemPrice(settled.getItemPrice());
        //payment.setDeliveryAddress(orderPlaced.getDeliveryAddress());
        //payment.setDeliveryPhoneNumber(orderPlaced.getDeliveryPhoneNumber());
        // payment.setOrderStatus(orderPlaced.getOrderStatus());
        payment.setOrderStatus("paid");
        SettlementApplication.applicationContext.getBean(bookdelivery.external.PaymentService.class)
            .pay(payment);
            System.out.println("페이먼트 생성" + payment.getOrderStatus());

    }

    public Long getSettlementid() {
        return settlementid;
    }

    public void setSettlementid(Long settlementid) {
        this.settlementid = settlementid;
    }
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderid(Long orderId) {
        this.orderId = orderId;
    }
    public Long getSellerid() {
        return sellerid;
    }

    public void setSellerid(Long sellerid) {
        this.sellerid = sellerid;
    }
    public Long getItemid() {
        return itemid;
    }

    public void setItemid(Long itemid) {
        this.itemid = itemid;
    }
    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
    public Integer getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(Integer itemPrice) {
        this.itemPrice = itemPrice;
    }
    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }




}
