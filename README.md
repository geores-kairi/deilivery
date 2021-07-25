# bookdelivery
Lv.2 Intensive Coursework - Kimg Sung-Hwan

<img src="https://user-images.githubusercontent.com/85722733/124438926-c0e43d80-ddb3-11eb-9d37-d89e8a7193eb.png"  width="50%" height="50%">

# 온라인 도서상점 (도서배송 서비스)

# Table of contents

- [조별과제 - 도서배송 서비스](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현)
    - [DDD 의 적용](#DDD-의-적용)
    - [동기식 호출과 Fallback 처리](#동기식-호출과-Fallback-처리)
    - [비동기식 호출과 Eventual Consistency](#비동기식-호출과-Eventual-Consistency)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [API 게이트웨이](#API-게이트웨이)
  - [운영](#운영)
    - [Deploy/Pipeline](#deploypipeline)
    - [동기식 호출 / Circuit Breaker / 장애격리](#동기식-호출-circuit-breaker-장애격리)
    - [Autoscale (HPA)](#Autoscale-(HPA))
    - [Zero-downtime deploy (Readiness Probe)](#Zerodowntime-deploy-(Readiness-Probe))
    - [ConfigMap](#ConfigMap)
    - [Self-healing (Liveness Probe)](#self-healing-(liveness-probe))


# 서비스 시나리오

기능적 요구사항
1. 고객이 도서를 선택하여 주문(Order)한다
2. 고객이 결제(Pay)한다
3. 결제가 완료되면 주문 내역이 도서상점에 전달된다(Ordermanagement)
4. 상점주인이 주문을 접수하고 도서를 포장한다
5. 도서 포장이 완료되면 상점소속배달기사가 배송(Delivery)을 시작한다.
6. 고객이 주문을 취소할 수 있다
7. 주문이 취소되면 배송 및 결제가 취소된다
8. 고객이 주문상태를 중간중간 조회한다
9. 주문/배송상태가 바뀔 때마다 고객이 마이페이지에서 상태를 확인할 수 있다.
10. 상점주인이 주문 접수 시 정산(Settlement) 시스템에 반영이 된다

# 추가 시나리오

### 11. 배송원이 배달완료 처리를 한다
### 12. 배달완료가 되면 자동으로 상점주인에게 정산이 된다
### 13. 정산이 되면 정산((Settlement)에 반영이 된다
### 14. 상점주인이 주문상태와 정산상태를 중간중간 조회를 한다


비기능적 요구사항
1. 트랜잭션
  - 결제가 완료되어야만 주문이 완료된다 (결제가 되지 않은 주문건은 아예 거래가 성립되지 않아야 한다 Sync 호출)
###   - 배달이 완료되어야만 정산이 된다 (배달이 되지 않은건은 정산이 성립되지 않아야 한다. Sync 호출)
2. 장애격리
  - 주문관리(Ordermanagement) 기능이 수행되지 않더라도 주문(Order)은 365일 24시간 받을 수 있어야 한다 Async (event-driven), Eventual Consistency 
###   - 정산(Settlement) 기능이 수행되지 않더라도 주문관리(Ordermanagement)은 365일 24시간 받을 수 있어야 한다 Async (event-driven), Eventual Consistency
###   - 결제시스템이 과중되면 사용자를 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다 Circuit breaker, fallback
3. 성능
  - 고객이 마이페이지에서 배송상태를 확인할 수 있어야 한다 CQRS
###   - 상점주인이 마이페이지에서 배송상태를 확인할 수 있어야 한다 CQRS


# 체크포인트

- 분석 설계


  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?
- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅: 
  - 무정지 운영 CI/CD (10)
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 
    - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?


# 분석/설계

## Event Storming 결과

* MSAEz 로 모델링한 이벤트스토밍 결과: http://www.msaez.io/#/storming/o3vDx7FSYPdgNQwy5allxyoOnYg2/1f14577a07cd2d737e2630e7659d476a


### 이벤트 도출

![1](https://user-images.githubusercontent.com/60598148/126855260-fae899b9-296a-431a-9a3a-74ef8e779ab3.jpg)


### 부적격 이벤트 탈락
![2](https://user-images.githubusercontent.com/60598148/126855262-6200c89f-31ff-448b-9494-48d04a5971f0.jpg)

    - 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
        - '주문내역이 상점에 전달됨' 및 '주문상태 업데이트됨'은 이벤트에 의한 반응에 가까우므로 이벤트에서 제외
        - '마이페이지에서 조회됨'은 발생한 사실, 결과라고 보기 어려우므로 이벤트에서 제외

### 액터, 커맨드 부착하여 읽기 좋게

![3](https://user-images.githubusercontent.com/60598148/126855442-db728ae0-55d2-4b4d-b3ae-2a035c85e532.jpg)

### 어그리게잇으로 묶기

![4](https://user-images.githubusercontent.com/60598148/126855529-4967f5dc-f2bf-46dd-92ee-7831bf2e1d9e.jpg)
    
    - 상점의 주문관리, 결제의 결제이력, 정산과 연결된 command 와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 묶어줌
    
### 바운디드 컨텍스트로 묶기

![5](https://user-images.githubusercontent.com/60598148/126855686-400b0e7a-0ef7-4950-a850-4185dd8aec39.jpg)

    - 도메인 서열 분리 
        - Core Domain:  ordermanagement : 없어서는 안될 핵심 서비스이며, 연견 Up-time SLA 수준을 99.999% 목표, 배포주기는 order의 경우 1주일 1회 미만, ordermanagement의 경우 1개월 1회 미만
        - Supporting Domain:  settlement : 점주 관리를 위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함
        - General Domain:   partner : 결제서비스로 3rd Party 외부 서비스를 사용하는 것이 경쟁력이 높음


### 폴리시의 부착과 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)

![6](https://user-images.githubusercontent.com/60598148/126855898-56383656-e0ad-4e2d-941b-eb616e83dbe3.jpg)


### 완성된 모형(팀과제)

![MSAEz](https://user-images.githubusercontent.com/85722733/124453306-36efa100-ddc2-11eb-9620-d07221ed7e78.png)

### 완성된 모형(개인과제 추가 모형)

![완성모델2](https://user-images.githubusercontent.com/60598148/126855923-0edca4e2-b665-466a-804b-4341ff2eb68d.jpg)


    - View Model 추가

### 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

![7](https://user-images.githubusercontent.com/60598148/126856031-ebd6aab4-b162-45f9-a5c5-ed24a9c8164b.jpg)


    - 상점주인이 주문을 접수한다 (ok)
    - 주문접수 정보가 정산시스템에 입력된다 (ok)
    - 배달원이 배달완료 정보를 입력한다 (ok)
    - 배달완료정보가 정산시스템에 반영된다 (ok)
    - 동시에 점주에게 정산내역이 지불된다 (ok)
    - 지불된 후 정산시스템에 정산내역이 반영된다 (ok)
  

### 비기능 요구사항에 대한 검증

![8](https://user-images.githubusercontent.com/60598148/126856304-ecb97e2e-6c2c-4956-94c5-e81e01cdc2db.jpg)



    - 마이크로서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
        - 배달 완료 시 정산처리:  배달완료가 되지 않은 건은 절대 정산하지 않는다는 경영자의 오랜 신념(?)에 따라, ACID 트랜잭션 적용. 주문완료시 결제처리에 대해서는 Request-Response 방식 처리
        - 배달 및 정산 상태에 대한 정보를 상점주인이 조회가능해야 하므로 각 이벤트에 대한 정보를 수신하여 CQRS로 조회 가능
        - 나머지 모든 inter-microservice 트랜잭션: 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함.




## 헥사고날 아키텍처 다이어그램 도출(추가 모델)
    
![9](https://user-images.githubusercontent.com/60598148/126884754-c82e469b-29e5-45da-8551-5191fa4ead3c.jpg)



    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 Pub/Sub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐

# 구현 

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 바운더리 컨텍스트 별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

```
cd ordermanagement
mvn spring-boot:run

cd settlement
mvn spring-boot:run 

cd partner
mvn spring-boot:run  
```

## DDD 의 적용

- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가? 

각 서비스 내에 도출된 핵심 Aggregate Root 객체를 Entity로 선언하였다. (주문관리(ordermgmt), 정산(settlement), 파트너(partner))

정산  Entity (Settlement.java)
```
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
        payment.setQty(settled.getQty());
        payment.setItemid(settled.getItemid());
        payment.setItemPrice(settled.getItemPrice());        
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
       .... 생략
```

Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 하였고 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다 

SettlementRepository.java
```
package bookdelivery;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.Optional;

@RepositoryRestResource(collectionResourceRel="settlements", path="settlements")
public interface SettlementRepository extends PagingAndSortingRepository<Settlement, Long>{    
    Optional<Settlement> findByOrderId(Long orderId);
}
```


- 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?

가능한 현업에서 사용하는 언어(유비쿼터스 랭귀지)를 모델링 및 구현 시 그대로 사용하려고 노력하였다.

- 적용 후 Rest API의 테스트

주문 결제 후 ordermgmts 주문 접수하기 POST
```
http POST http://localhost:8082/ordermgmts orderId=1 itemId=1 orderStatus="orderTaken"
```
![10](https://user-images.githubusercontent.com/60598148/126857231-660cbeec-0425-479f-9e30-13c371392de7.jpg)


배달완료 하기 PUT 
```
http PUT http://localhost:8082/ordermgmts/1 orderId=1 itemId=1 orderStatus="finished"
```
![11](https://user-images.githubusercontent.com/60598148/126857302-9ec7c80f-8a10-4770-9565-af4c702ec6f7.jpg)



## 동기식 호출과 Fallback 처리 
(Request-Response 방식의 서비스 중심 아키텍처 구현)

- 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)

요구사항대로 배달완료로 업데이트되어야만 지불 서비스를 호출할 수 있도록 주문 시 지불 처리를 동기식으로 호출하도록 한다. 

Settlement.java Entity Class에 @PostUpdate로 배달완료 직후 지불을 호출하도록 처리하였다
```
@PostUpdate
    public void onPostUpdate(){

        Settled settled = new Settled();
        BeanUtils.copyProperties(this, settled);
        settled.publishAfterCommit();

        bookdelivery.external.Payment payment = new bookdelivery.external.Payment();
        //mappings goes here
        payment.setSettlementid(settled.getSettlementid());
        payment.setOrderid(settled.getOrderId());       
        payment.setQty(settled.getQty());
        payment.setItemid(settled.getItemid());
        payment.setItemPrice(settled.getItemPrice());       
        payment.setOrderStatus("paid");
        SettlementApplication.applicationContext.getBean(bookdelivery.external.PaymentService.class)
            .pay(payment);
            System.out.println("페이먼트 생성" + payment.getOrderStatus());

    }
```
동기식 호출은 PaymentService 클래스를 두어 FeignClient 를 이용하여 호출하도록 하였다.

PaymentService.java

```
package bookdelivery.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="payment", url="localhost:8086", fallback = PaymentServiceFallback.class)
public interface PaymentService {

    @RequestMapping(method= RequestMethod.POST, path="/payments")
    public void pay(@RequestBody Payment payment);

}
```
동기식 호출로 인하여, 지불 서비스에 장애 발생 시(서비스 다운) 주문관리 서비스에도 장애가 전파된다는 것을 확인

Ordermanagement, Settlement 서비스 구동 & Payment 서비스 다운 되어 있는 상태에서는 주문 생성 시 오류 발생

![11](https://user-images.githubusercontent.com/60598148/126857750-d5c6a7df-a604-4d65-a5c8-c4abb7b9fac7.jpg)


--> Payment 서비스 구동하여 주문 재생성 시 정상적으로 생성됨
![12](https://user-images.githubusercontent.com/60598148/126857758-ab7c1376-750c-4aa9-9cdc-8765ea71d618.jpg)


- 서킷브레이커를 통하여 장애를 격리시킬 수 있는가?

정산-지불 Req-Res구조에서 FeignClient 및 Spring Hystrix 를 사용하여 Fallback 기능을 구현하였다

정산 서비스의 application.yml 파일에 feign.hystrix.enabled: true 로 활성화시킨다

```
feign:
  hystrix:
    enabled: true
```
PaymentService 에 feignClient fallback 옵션을 추가하였고 이를 위해 PaymentServiceFallback 클래스를 추가하였다

PaymentService.java
```
package bookdelivery.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="payment", url="localhost:8086", fallback = PaymentServiceFallback.class)
public interface PaymentService {

    @RequestMapping(method= RequestMethod.POST, path="/payments")
    public void pay(@RequestBody Payment payment);

}
```
PaymentServiceFallback.java
```
package bookdelivery.external;

import org.springframework.stereotype.Component;

@Component
public class PaymentServiceFallback implements PaymentService{

  @Override
  public void pay(Payment payment) {
    System.out.println("Circuit breaker has been opened. Fallback returned instead.");
  }

}

```
fallback 기능 없이 payment 서비스를 중지하고 주문 생성 시에는 오류가 발생했으나, 

위와 같이 fallback 기능 활성화 후에는 payment서비스가 동작하지 않더라도 주문 생성 시에 오류가 발생하지 않는다

![12](https://user-images.githubusercontent.com/60598148/126857763-a5cef1df-c167-4ef4-af39-c05678aaf6a6.jpg)
![13](https://user-images.githubusercontent.com/60598148/126857766-fc173028-c51c-409e-b543-11463e4f405d.jpg)


위와 같이 fallack 옵션이 동작하여 "Circuit breaker has been opened. Fallback returned instead." 로그가 보여진다


## 비동기식 호출과 Eventual Consistency 
(이벤트 드리븐 아키텍처)

- 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?

- Correlation-key: 각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?

카프카를 이용하여 배달완료 시 지불 처리를 제외한 나머지 모든 마이크로서비스 트랜잭션은 Pub/Sub 관계로 구현하였다. 

아래는 배달완료 이벤트(OrderFinished)를 카프카를 통해 주문관리(settlement) 서비스에 연계받는 코드 내용이다. 

ordermgmt 서비스에서는 배달원이 배달완료 시 PostUpdate로 Orerfinished 이벤트를 발생시키고,
```
public class Ordermgmt {
    @PostUpdate
    public void onPostUpdate(){

        if (this.orderStatus.equals("finished")){
            OrderFinished orderFinished = new OrderFinished();
            BeanUtils.copyProperties(this, orderFinished);
            orderFinished.publishAfterCommit();            
            bookdelivery.external.Payment payment = new bookdelivery.external.Payment();

        }
        else{
        CancelOrderTaken cancelOrderTaken = new CancelOrderTaken();
        BeanUtils.copyProperties(this, cancelOrderTaken);
        cancelOrderTaken.publishAfterCommit();
        }
    }
```

settlement 서비스에서는 카프카 리스너를 통해 ordermgmt의 OrderFinished 이벤트를 수신받아서 폴리시(updateSettlement) 처리하였다. (getOrderId()를 호출하여 Correlation-key 연결)
```
@Service
public class PolicyHandler{
 @StreamListener(KafkaProcessor.INPUT)
    
    public void wheneverOrderFinished_UpdateSettlement(@Payload OrderFinished orderfinished){

        //System.out.println("리스너 체크");

        if(!orderfinished.validate()) return;

        // Sample Logic //
        // Settlement settlement = new Settlement();
        // settlementRepository.save(settlement);

        settlementRepository.findByOrderId(orderfinished.getOrderId()).ifPresent(settlement->{           
            settlement.setOrderStatus("deliveryfinished");//add
            settlementRepository.save(settlement);
        });         
    }
  }
```



- Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가?

정산(settlement)서비스의 포트 추가(기존:8085, 추가:8087)하여 2개의 노드로 배송서비스를 실행한다. bookdelivery topic의 partition은 1개이기 때문에 기존 8085 포트의 서비스만 partition이 할당된다.

주문관리서비스(ordermanagement)에서 이벤트가 발생하면 8085포트에 있는 settlement서비스에게만 이벤트 메세지가 수신되게 된다.
![15](https://user-images.githubusercontent.com/60598148/126858337-3f60fad2-bd92-422f-b6ae-ea5b26e138ce.jpg)

8087포트의 delivery서비스의 경우 메세지를 수신받지 못한다.

8085 포트를 중지 시키면 8087포트의 settlement 서비스에서 partition을 할당받는다
![16](https://user-images.githubusercontent.com/60598148/126858341-c30ab742-bde0-46cf-8a6b-a2c32a12cdb4.jpg)


### SAGA 패턴
- 취소에 따른 보상 트랜잭션을 설계하였는가(Saga Pattern)

SAGA 패턴은 각 서비스의 트랜잭션 완료 후에 다음 서비스가 트리거 되어 트랜잭션을 실행하는 방법으로

현재 BookDelivery 시스템도 SAGA 패턴으로 설계되어 있다.

#### SAGA 패턴에 맞춘 트랜잭션 실행

![17](https://user-images.githubusercontent.com/60598148/126858544-c7a397e0-cf7f-458b-9573-a60f4cb79bef.jpg)


ordermgmt 서비스의 배달완료로 입력을 받으면 ordermgmt의 orderStatus가 갱신이 되며, settlement에서는 해당 주문건의 상태를 업데이트한다.

실행한 결과는 아래와 같다

![18](https://user-images.githubusercontent.com/60598148/126858780-67360264-8440-4083-8440-92cbc4da3263.jpg)

![19](https://user-images.githubusercontent.com/60598148/126858785-c9dbe7d7-e744-4756-ad02-10d4073f0346.jpg)

![23](https://user-images.githubusercontent.com/60598148/126858815-5d145955-d3bf-420e-8901-129d14410a9b.jpg)

배달완료 시 결국 지불이 발생하여 지불이 완료되며, 

![21](https://user-images.githubusercontent.com/60598148/126858852-b9e93ef9-a962-4a9e-817e-a79c935d1724.jpg)

이를 settelment 서비스에서 연계받아 지불내역을 수신받게 된다

![22](https://user-images.githubusercontent.com/60598148/126858866-c632c0c9-b375-415c-875f-f17af03fcf12.jpg)


### CQRS
- CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

배송상태변경/정산지급 될때 점주가 마이페이지에서 상태를 확인할 수 있어야 한다는 요구사항에 따라 주문 서비스 내에 MyPage View를 모델링하였다

![24](https://user-images.githubusercontent.com/60598148/126884874-627ba888-3706-4c04-b1ce-f799111a68b3.jpg)


주문에 대한 접수(orderTaken) 시 orderId를 키값으로 MyPage 데이터도 생성되며 

"배송완료, 정산지급"의 이벤트에 따라 정산상태가 업데이트되도록 모델링하였다

MySettlement CQRS처리를 위해 주문, 결제, 주문관리, 배송 서비스와 별개로 조회를 위한 MySettlement_table 테이블이 생성된다

MySettlement.java : 엔티티 클래스
```
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

```
MySettlementRepository.java : 퍼시스턴스
```
package bookdelivery;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.repository.PagingAndSortingRepository;
import java.util.List;
import java.util.Optional;



//@RepositoryRestResource(collectionResourceRel="Mysettlements", path="Mysettlements")
public interface MySettlementRepository extends CrudRepository<MySettlement, Long> {
    Optional<MySettlement> findByOrderId(Long orderId);
}

```
MySettlementViewHandler.java : 아래와 같이 결제완료를 통한 MySettlement 정산 데이터 생성 및 상태 변경에 대한 이벤트 수신 처리부가 있다


```
--주문접수 이벤트

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
    
-- 배송출발 이벤트

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
    
-- 배송완료(주문완료) 이벤트

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

-- 정산지급 이벤트

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
```

CQRS 테스트

주문접수 시 주문 정상 접수 등록됨을 확인

![25](https://user-images.githubusercontent.com/60598148/126885017-32745680-2415-4dda-8f4c-158cc49962be.jpg)

배달완료 시 정산이 바로 지급되므로 정상적으로 정산 지급된 상태임을 확인

![26](https://user-images.githubusercontent.com/60598148/126885028-651c2530-c8bf-442b-a470-af9ed194110b.jpg)



- Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?

ordermanagement 서비스만 구동되고 settlement 서비스는 멈춰있는 상태이다. 주문관리에 이벤트가 발생하면 카프카 큐에 정상적으로 들어감을 확인할 수 있다.

주문관리 이벤트 생성

![27](https://user-images.githubusercontent.com/60598148/126886477-e174a058-7803-4979-9243-d89eca197604.jpg)


카프카 Consumer 캡쳐

![28](https://user-images.githubusercontent.com/60598148/126886483-f83d9aa0-65b4-4cb6-be34-a031a509b280.jpg)


정산(settlement)서비스 실행 및 실행 후 카프카에 적재된 메세지 수신 확인

![29](https://user-images.githubusercontent.com/60598148/126886519-1b902b3b-e37a-4ddd-b346-05653706df32.jpg)



## 폴리글랏 퍼시스턴스

- 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?

payment 서비스의 경우 타 서비스들의 비해 안정성이 중요하다고 생각하였다. H2 DB의 경우 대규모 주문이 발생시 안정성과 성능이 아직은 부족하다고 생각했다. 그래서 안정성과 성능이 높은 DB와 경제성(라이센스 비용)에 강점이 있는 MySQL DB를 선택하게 되었다.

Payment서비스 pom.xml 의존성을 변경해 주었다.

![31](https://user-images.githubusercontent.com/60598148/126889319-51500f35-c5ad-4fef-ab43-20d5ce947aac.jpg)


application.yml 파일에 dababase 속성도 넣어주었다.

![30](https://user-images.githubusercontent.com/60598148/126889321-0f96845b-c15b-4208-8130-7b9ccbf37362.jpg)


payment 서비스를 트리거 하여 MySQL DB에 정상적으로 insert 확인

http PUT http://localhost:8082/ordermgmts/1 orderId=2 itemId=2 orderStatus="finished"

![32](https://user-images.githubusercontent.com/60598148/126889334-15230fcc-0c2b-4aad-be89-f406d6d14728.jpg)



## API 게이트웨이

- API GW를 통하여 마이크로 서비스들의 진입점을 통일할 수 있는가?

아래는 gateway 서비스의 application.yml이며, 마이크로서비스들의 진입점을 통일하여 URL Path에 따라서 마이크로서비스별 서로 다른 포트로 라우팅시키도록 설정되었다.

gateway 서비스의 application.yml 파일 

![33](https://user-images.githubusercontent.com/60598148/126889525-dc1cc990-35b5-4ffc-bb2c-36ad9dc7556c.jpg)


Gateway 포트인 8088을 통해서 주문접수를 생성시켜 8082 포트에서 서비스되고 있는 주문관리서비스(ordermgmt)가 정상 동작함을 확인함

![34](https://user-images.githubusercontent.com/60598148/126889581-8b1c1915-5c61-4db6-b28b-647c36605d71.jpg)




# 운영
## Deploy/Pipeline
(CI/CD 설정)
**BuildSpec.yml 사용**
각 MSA 구현물은 git의 source repository 에 구성되었고, AWS의 CodeBuild를 활용하여 무정지  CI/CD를 설정하였다.

CodeBuild 설정
- 빌드 프로젝드 생성(각 MSA별 별도 설정)

![1](https://user-images.githubusercontent.com/60598148/125281723-88afa280-e351-11eb-8956-dea1b984e804.jpg)

- 기본 repository 

![2](https://user-images.githubusercontent.com/60598148/125281985-d6c4a600-e351-11eb-9a6c-1009c33aa28a.jpg)

- 빌드 환경 설정

환경변수(KUBE_URL, KUBE_TOKEN, repository 등 설정)

![3](https://user-images.githubusercontent.com/60598148/125282230-18ede780-e352-11eb-9b96-43a3eb6b0a05.jpg)

- 빌드 스펙

![4](https://user-images.githubusercontent.com/60598148/125282499-6c603580-e352-11eb-8948-d539048971b6.jpg)

buildspec.yml 파일 내용
![5](https://user-images.githubusercontent.com/60598148/125282795-bba66600-e352-11eb-9c70-b790bb6b567c.jpg)

- 빌드 결과

![6](https://user-images.githubusercontent.com/60598148/125283156-19d34900-e353-11eb-94d2-e7b197cf0dfd.jpg)
![7](https://user-images.githubusercontent.com/60598148/125283401-5c952100-e353-11eb-9c64-943ee4766263.jpg)

![8](https://user-images.githubusercontent.com/60598148/125283634-8fd7b000-e353-11eb-8200-768c23ad2f77.jpg)


## 동기식 호출 / Circuit Breaker / 장애격리
서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함  
오더 요청이 과도할 경우 서킷 브레이크를 통해 장애 격리를 하려고 한다. 

Hystrix 를 설정: 요청처리 쓰레드에서 처리시간이 610 ms가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정  
![image](https://user-images.githubusercontent.com/85722738/125285997-3c1a9600-e356-11eb-9c05-119e694a38c5.png)


결제 서비스의 부하 처리 - 400 ms에서 증감 220 ms 정도 수준으로 설정  
![image](https://user-images.githubusercontent.com/85722738/125285881-1ab9aa00-e356-11eb-9ed3-740c6e2bcafe.png)


부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인: 동시사용자 100명 60초 동안 실시  
![image](https://user-images.githubusercontent.com/85722738/125383279-25198980-e3d2-11eb-948a-881c61c88a01.png)

요청 상태에 따라 회로 열기/닫기가 반복되는 모습 확인
![image](https://user-images.githubusercontent.com/85722738/125383229-13d07d00-e3d2-11eb-81f9-425bdec581d5.png)
![image](https://user-images.githubusercontent.com/85722738/125383434-5b570900-e3d2-11eb-971e-f7ae5da0c6ba.png)


## Autoscale (HPA)
주문 서비스에 HPA를 설정한다. 평균대비 CPU 20퍼 초과시 3개까지 pod 추가  
![image](https://user-images.githubusercontent.com/85722738/125292390-60c63c00-e35d-11eb-8e39-f5597eeec376.png)

현재 주문서비스 pod 상태 확인  
![image](https://user-images.githubusercontent.com/85722738/125292058-06c57680-e35d-11eb-96c6-42da212b0306.png)

siege 로 부하테스트를 진행  
![image](https://user-images.githubusercontent.com/85722738/125292601-94a16180-e35d-11eb-980e-7427c462f7ca.png)

아래와 같이 scale out 되는것을 확인할 수 있다.  
![image](https://user-images.githubusercontent.com/85722738/125293099-0da0b900-e35e-11eb-91bc-72ab25ba08fe.png)


## Zero-downtime deploy (Readiness Probe)
(무정지 배포)
서비스의 무정지 배포를 위하여 주문관리(Ordermanagement) 서비스의 배포 yaml 파일에 readinessProbe 옵션을 추가하였다.
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ordermanagement
  labels:
    app: ordermanagement
spec:
  replicas: 2
  selector:
    matchLabels:
      app: ordermanagement
  template:
    metadata:
      labels:
        app: ordermanagement
    spec:
      containers:
      - name: ordermanagement
        image: 879772956301.dkr.ecr.ap-northeast-1.amazonaws.com/user03-ordermgmt:latest
        ports:
        - containerPort: 8080
        readinessProbe:
          httpGet:
            path: '/ordermgmts'
            port: 8080
          initialDelaySeconds: 10
          timeoutSeconds: 2
          periodSeconds: 5
          failureThreshold: 3    
```

```
]root@labs--679458944:/home/project# kubectl apply -f deployment.yaml
deployment.apps/ordermanagement created

]root@labs--679458944:/home/project# kubectl expose deployment/ordermanagement
service/ordermanagement exposed
```
![readiness](https://user-images.githubusercontent.com/85722733/125400678-273d1180-e3ed-11eb-854d-a7617b8aaa2b.png)

siege 를 통해 100명의 가상의 유저가 30초동안 주문관리 서비스를 지속적으로 호출하게 함과 동시에
```
siege -c100 -t30S -v --content-type "application/json" 'http://ab7cbdfab34934e4daefe25f88a22d77-556791783.ap-northeast-1.elb.amazonaws.com:8080/ordermgmts POST {"orderId": "1", "itemName": "ITbook", "qty": "3", "customerName": "HeidiCho", "deliveryAddress": "kyungkido sungnamsi", "deliveryPhoneNumber": "01012341234", "orderStatus": "orderTaken"}'
```
kubectl set image 명령어를 통해 배포를 수행하였다.
![readiness2](https://user-images.githubusercontent.com/85722733/125286095-5a809180-e356-11eb-9bfb-a13f663478cf.png)

siege 테스트 결과 연결시도 대비 성공률이 100% 로서 readinessProbe 옵션을 통해 무정지 배포를 확인하였다.
![readiness3](https://user-images.githubusercontent.com/85722733/125286133-666c5380-e356-11eb-9d99-521f156426ce.png)

## ConfigMap
운영환경에서 컨피그맵을 통해 pod 생성 시 정해진 kafka url 과 log 파일 설정(운영과 개발 분리)

bookdelivery-config.yml

![14](https://user-images.githubusercontent.com/60598148/125390104-3e740300-e3dd-11eb-9218-89f36a3416d2.jpg)

컨피그맵 생성 및 확인

![15](https://user-images.githubusercontent.com/60598148/125390157-55b2f080-e3dd-11eb-8f69-1d426c0ed830.jpg)

deployment yaml 파일

       - name: consumer
          image: 052937454741.dkr.ecr.ap-northeast-2.amazonaws.com/lecture-consumer:latest 
          env:
          - name: KAFKA_URL
            valueFrom:
              configMapKeyRef:
                name: kafka-config
                key: KAFKA_URL
          - name: LOG_FILE
            valueFrom:
              configMapKeyRef:
                name: kafka-config
                key: LOG_FILE

POD  생성 후 아래 명령어를 통해 pod 내부 환경 조회
kubectl exec -it bookdelivery-nstest-deployment-6f976bf7df-kl5mz -- /bin/sh

![16](https://user-images.githubusercontent.com/60598148/125390906-8d6e6800-e3de-11eb-81fa-7c04f21415c5.jpg)

configmap value 정상 반영 확인됨

프로그램(python) 파일 반영을 통해 kafka 로그 확인

from kafka import KafkaConsumer
from logging.config import dictConfig
import logging
import os

kafka_url = os.getenv('KAFKA_URL')
log_file = os.getenv('LOG_FILE')

consumer = KafkaConsumer('lecture', bootstrap_servers=[
                         kafka_url], auto_offset_reset='earliest', enable_auto_commit=True, group_id='alert')



## Self-healing (Liveness Probe)

주문관리(Ordermanagement) 서비스의 배포 yaml 파일에 Pod 내 /tmp/healthy 파일을 5초마다 체크하도록 livenessProbe 옵션을 추가하였다

```
apiVersion: v1
kind: Pod
metadata:
  name: ordermanagement
  labels:
    app: ordermanagement
spec:
  containers:
  - name: ordermanagement
    image: 879772956301.dkr.ecr.ap-northeast-1.amazonaws.com/user03-ordermgmt:latest
    livenessProbe:
      exec:
        command:
        - cat 
        - /tmp/healthy
      initialDelaySeconds: 15
      periodSeconds: 5
```
yaml 파일을 실행하여 주문관리 pod 가 생성되었다
```
]root@labs--679458944:/home/project# kubectl create -f test_liveness.yaml
pod/ordermanagement created
```
![live0](https://user-images.githubusercontent.com/85722733/125304329-77be5b80-e368-11eb-8eeb-a2083a26552b.png)

Pod 구동 시 Running 상태이나 Pod 내 체크 대상인 /tmp/healthy 파일이 없기 때문에 livenessProbe 옵션의 "Self-healing" 특징 대로 계속 Retry하여 Restart 된 것이 확인된다

kubectl describe 명령어로 주문관리 Pod 상태 확인 시 livenessProbe 관련 실패 로그

![live2](https://user-images.githubusercontent.com/85722733/125304502-9d4b6500-e368-11eb-80e3-4bce6f898fc7.png)

주문관리 Pod 내부로 진입하여 touch 명령어를 통해 /tmp/healthy 파일 생성 시 Restart가 3번째에서 중단되고 Pod가 정상 동작함을 확인하였다 (2회 Fail 후 파일 생성되어 3번째에 성공)

![live1](https://user-images.githubusercontent.com/85722733/125304569-ac321780-e368-11eb-8fc4-92ab83995d2a.png)

![live3](https://user-images.githubusercontent.com/85722733/125304613-b3f1bc00-e368-11eb-9a8c-01a897ab7ccf.png)
