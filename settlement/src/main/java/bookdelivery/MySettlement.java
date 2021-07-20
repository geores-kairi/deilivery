package bookdelivery;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="MySettlement_table")
public class MySettlement {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long mysettleid;
        private Long orderId;
        private Long itemId;
        private Integer qty;
        private Integer itemPrice;
        private String orderStatus;

        public Long getmysettleid() {
            return mysettleid;
        }

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }
        
        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
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
