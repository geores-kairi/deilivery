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
    @Autowired PaymentRepository paymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCancelOrderTaken_CancelPay(@Payload CancelOrderTaken cancelOrderTaken){

        if(!cancelOrderTaken.validate()) return;

        System.out.println("\n\n##### listener CancelPay : " + cancelOrderTaken.toJson() + "\n\n");

        // Logic //
        // Payment payment = new Payment();
        // paymentRepository.save(payment);
        
        // Logic //결제취소로 업데이트해줘야 PayCanceled 이벤트 호출
        //Payment payment = new Payment();
        //payment.setOrderId(cancelOrderTaken.getId()); 
        //payment.setOrderStatus("payCanceled"); 
        //paymentRepository.save(payment);

        paymentRepository.findByOrderId(cancelOrderTaken.getOrderId()).ifPresent(ordermgmt->{
            ordermgmt.setOrderStatus("orderFinallyCanceled");//add
            paymentRepository.save(ordermgmt);
        });
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
