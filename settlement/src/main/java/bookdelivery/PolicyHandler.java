package bookdelivery;

import bookdelivery.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired SettlementRepository settlementRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderTaken_takeSettleBasic(@Payload OrderTaken orderTaken){

        if(!orderTaken.validate()) return;

        System.out.println("\n\n##### listener takeSettleBasic : " + orderTaken.toJson() + "\n\n");

        // Sample Logic //
        Settlement settlement = new Settlement();
       // settlement.setSettlementid(orderTaken.getSettlementid());
        settlement.setOrderid(orderTaken.getOrderId());
        settlement.setItemid(orderTaken.getItemId());                             
        //settlement.setSellerid(orderfinished.getSellerid());  
        settlement.setOrderStatus(orderTaken.getOrderStatus());
        settlementRepository.save(settlement);     
    }

    @StreamListener(KafkaProcessor.INPUT)
    
    public void wheneverOrderFinished_UpdateSettlement(@Payload OrderFinished orderfinished){

        //System.out.println("리스너 체크");

        if(!orderfinished.validate()) return;

        System.out.println("\n\n##### listener UpdateSettlement : " + orderfinished.toJson() + "\n\n");



        // Sample Logic //
        // Settlement settlement = new Settlement();
        // settlementRepository.save(settlement);

        settlementRepository.findByOrderId(orderfinished.getOrderId()).ifPresent(settlement->{

            System.out.println("\n\n##### getorderid: "+ orderfinished.getOrderId());
            settlement.setOrderStatus("deliveryfinished");//add
            settlementRepository.save(settlement);
        });         
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaid_PayRecivement(@Payload Paid paid){

        System.out.println("지불 체크");

        if(paid.validate()) return;

        System.out.println("\n\n##### listener 지불 : " + paid.toJson() + "\n\n");



        // Sample Logic //
        // Settlement settlement = new Settlement();
        // settlementRepository.save(settlement);

        settlementRepository.findByOrderId(paid.getOrderId()).ifPresent(settlement->{

            System.out.println("\n\n##### getorderid: "+ paid.getOrderId());
            settlement.setOrderStatus("paid");//add
            settlementRepository.save(settlement);
        });         
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
