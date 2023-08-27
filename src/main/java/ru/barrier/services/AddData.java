package ru.barrier.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.barrier.models.Payment;
import ru.barrier.models.User;
import ru.barrier.models.UserBarrier;
import ru.barrier.repository.PaymentRepository;
import ru.barrier.repository.UserRepository;
import java.util.TreeSet;

@Component
public class AddData {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public void registerUser(Long chatId) {
        User user = new User();
        user.setChatId(chatId);
        userRepository.save(user);
    }

    public void newPayment(Long chatId, String idPayment){
        Payment payment = new Payment();
        payment.setChatId(chatId);
        payment.setIdPayment(idPayment);
        paymentRepository.save(payment);
    }
    @Transactional
    public void newUserBarrier(Long chatId, Integer place) {
        User user = new User();
        UserBarrier userBarrier = new UserBarrier();

        user.setChatId(chatId);
        userBarrier.setChatId(chatId);
        userBarrier.setParkingPlace(place);
        userBarrier.setStoppedBy(0);
        user.setUserBarrier(userBarrier);
        userBarrier.setUser(user);

        userRepository.save(user);
    }

    @Transactional
    public void stoppedByT(User user, int stoppedBy) {
        UserBarrier userBarrier = user.getUserBarrier();

        user.setChatId(user.getChatId());
        userBarrier.setChatId(userBarrier.getChatId());
        userBarrier.setStoppedBy(stoppedBy);

        user.setUserBarrier(userBarrier);
        userBarrier.setUser(user);
        userRepository.save(user);

    }

    @Transactional
    public void newUserTest() {
        TreeSet states = new TreeSet<Integer>();
        int d = 1;
        boolean e = states.contains(d);

        boolean setParkingPlaceLocal = false;
        int placeRandom = 0;

        for (int i = 0; i < 27; i++) {
            User user = new User();
            UserBarrier userBarrier = new UserBarrier();

            user.setChatId(1392677678 + i);

            userBarrier.setChatId(1392677678 + i);

            setParkingPlaceLocal = true;
            while (setParkingPlaceLocal != false) {
                placeRandom = (int) ((Math.random() * (28 - 1)) + 1);
                setParkingPlaceLocal = states.contains(placeRandom);
//                System.out.println(placeRandom + "   " + setParkingPlaceLocal);
            }
            states.add(placeRandom);


            userBarrier.setParkingPlace(placeRandom);

            user.setUserBarrier(userBarrier);
            userBarrier.setUser(user);

            userRepository.save(user);
        }
    }

}
