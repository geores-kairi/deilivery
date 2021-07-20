package bookdelivery;

import bookdelivery.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class MySettlementViewHandler {


    @Autowired
    private MySettlementRepository mySettlementRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderTaken_then_CREATE_1 (@Payload OrderTaken OrderTaken) {
        try {

            if (!OrderTaken.validate()) return;

            // view 객체 생성
            MySettlement mySettlement = new MySettlement();
            // view 객체에 이벤트의 Value 를 set 함
            mySettlement.setOrderId(OrderTaken.getOrderId());
            mySettlement.setItemId(OrderTaken.getItemId());
            //myPage.setCustomerName(OrderTaken.getCustomerName());
            //myPage.setItemName(OrderTaken.getItemName());
            mySettlement.setQty(OrderTaken.getQty());
            //mySettlement.setItemPrice(OrderTaken.getItemPrice());
            mySettlement.setOrderStatus(OrderTaken.getOrderStatus());
            // view 레파지 토리에 save
            mySettlementRepository.save(mySettlement);

            System.out.println("\n\n##### mysettlement 마이세틀먼트리포지토리0 : " +mySettlementRepository.toString()); 
        
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryStarted_then_UPDATE_2(@Payload DeliveryStarted DeliveryStarted) {
        
        try {
            if (!DeliveryStarted.validate()) return;
                // view 객체 조회 
            Optional<MySettlement> mySettlementOption = mySettlementRepository.findByOrderId(DeliveryStarted.getOrderId()); 
            if( mySettlementOption.isPresent()) {                
                MySettlement mySettlement = mySettlementOption.get();
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                    mySettlement.setOrderStatus(DeliveryStarted.getOrderStatus());
                // view 레파지 토리에 save
                mySettlementRepository.save(mySettlement);
                System.out.println("\n\n##### mysettlement 배송출발3 오더상태: " + mySettlement.getOrderStatus() + "\n\n");
            }            
        }catch (Exception e){
            e.printStackTrace();
        }
    }    

    @StreamListener(KafkaProcessor.INPUT)
    public void whenSettled_then_UPDATE_2(@Payload OrderFinished OrderFinished) {
        try {
            if (!OrderFinished.validate()) return;
                // view 객체 조회
            Optional<MySettlement> mySettlementOption = mySettlementRepository.findByOrderId(OrderFinished.getOrderId());  
            if( mySettlementOption.isPresent()) {                
                MySettlement mySettlement = mySettlementOption.get();
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                    mySettlement.setOrderStatus(OrderFinished.getOrderStatus());
                // view 레파지 토리에 save
                mySettlementRepository.save(mySettlement);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenSettled_then_UPDATE_3(@Payload Paid paid) {
        try {
            if (!paid.validate()) return;
                // view 객체 조회
            Optional<MySettlement> mySettlementOption = mySettlementRepository.findByOrderId(paid.getOrderId());  
            if( mySettlementOption.isPresent()) {                
                MySettlement mySettlement = mySettlementOption.get();
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                    mySettlement.setOrderStatus(paid.getOrderStatus());
                // view 레파지 토리에 save
                mySettlementRepository.save(mySettlement);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

