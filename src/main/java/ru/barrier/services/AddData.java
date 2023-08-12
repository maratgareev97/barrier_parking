package ru.barrier.services;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.barrier.models.Payment;
import ru.barrier.models.User;
import ru.barrier.models.UserBarrier;
import ru.barrier.repository.PaymentRepository;
import ru.barrier.repository.UserBarrierRepository;
import ru.barrier.repository.UserRepository;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

@Component
public class AddData {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserBarrierRepository userBarrierRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public void newUserBarrier(Long chatId, Integer place) {
        User user = new User();
        UserBarrier userBarrier = new UserBarrier();

        user.setChatId(chatId);
        userBarrier.setChatId(chatId);
        userBarrier.setParkingPlace(place);
        user.setUserBarrier(userBarrier);
        userBarrier.setUser(user);

        userRepository.save(user);

    }

    public void newPayment(Long chatId, Integer parkingPlace, Integer amountOfDays) {
        UserBarrier userBarrier = new UserBarrier();
        userBarrier.setChatId(chatId);
        userBarrier.setDateTimeLastPayment(LocalDateTime.now());
        userBarrier.setDateTimeNextPayment(LocalDateTime.now().plusDays(amountOfDays));
        userBarrier.setParkingPlace(parkingPlace);
        userBarrier.setAmountOfDays(amountOfDays);
        userBarrierRepository.save(userBarrier);

        Payment payment = new Payment();
        payment.setChatId(chatId);
        payment.setDateTimePayment(LocalDateTime.now());
        paymentRepository.save(payment);

        LocalDateTime localDateTime = LocalDateTime.of(2023, Month.MAY, 12, 22, 12, 30);
        LocalDateTime localDateTime1 = LocalDateTime.of(2023, Month.MAY, 12, 22, 12, 30);
        System.out.println(localDateTime1.compareTo(localDateTime1));
        System.out.println(LocalDateTime.of(2023, Month.MAY, 12, 22, 12, 30));


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String ttt = "2016-05-11 00:46";
        LocalDateTime start = LocalDateTime.parse(ttt, formatter);
        LocalDateTime end = LocalDateTime.parse("2016-05-10 12:26", formatter);

        Duration duration = Duration.between(start, end);

        System.out.printf(
                "%dд %dч %dмин%n",
                duration.toDays(),
                duration.toHours() % 24,
                duration.toMinutes() % 60
        );

    }

//    public void newPaymentTest(Long chatId){
//        UserBarrier userBarrier = new UserBarrier();
//
//        userBarrier.setChatId(chatId);
//
//        Payment payment1 = new Payment();
//        payment1.setChatId(chatId);
//        payment1.setUserBarrier(userBarrier);
//        Payment payment2 = new Payment();
//        payment2.setChatId(123);
//        payment2.setUserBarrier(userBarrier);
//        Payment payment3 = new Payment();
//        payment3.setChatId(234);
//        payment3.setUserBarrier(userBarrier);
//
////        userBarrier.getPayments().add(payment1);
////        userBarrier.getPayments().add(payment2);
////        userBarrier.getPayments().add(payment3);
//
////        userBarrier.setPayments((List<Payment>) payment);
//
//        userBarrierRepository.save(userBarrier);
//        paymentRepository.save(payment1);
//        paymentRepository.save(payment2);
//        paymentRepository.save(payment3);
//
//
//
//    }

    public void newUserTest() {
        TreeSet states = new TreeSet<Integer>();
        int d = 1;
        boolean e = states.contains(d);

        boolean setParkingPlaceLocal = false;
        int placeRandom = 0;

        for (int i = 0; i < 12; i++) {
            User user = new User();
            UserBarrier userBarrier = new UserBarrier();

            user.setChatId(1392677678 + i);

            userBarrier.setChatId(1392677678 + i);

            setParkingPlaceLocal = true;
            while (setParkingPlaceLocal != false) {
                placeRandom = (int) ((Math.random() * (28 - 1)) + 1);
                setParkingPlaceLocal = states.contains(placeRandom);
                System.out.println(placeRandom + "   " + setParkingPlaceLocal);
            }
            states.add(placeRandom);


            userBarrier.setParkingPlace(placeRandom);

            user.setUserBarrier(userBarrier);
            userBarrier.setUser(user);

            userRepository.save(user);
        }
    }
}