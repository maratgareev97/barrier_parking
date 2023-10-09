package ru.barrier.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.barrier.models.*;
import ru.barrier.models.Payment;
import ru.barrier.repository.AdminUsersRepository;
import ru.barrier.repository.CashPaymentRepository;
import ru.barrier.repository.PaymentRepository;
import ru.barrier.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.TreeSet;

@Component
public class AddData {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CashPaymentRepository cashPaymentRepository;

    @Autowired
    private AdminUsersRepository adminUsersRepository;

    public void registerUser(Long chatId) {
        User user = new User();
        user.setChatId(chatId);
        userRepository.save(user);
    }

    public void newPayment(Long chatId, String idPayment) {
        Payment payment = new Payment();
        payment.setChatId(chatId);
        payment.setIdPayment(idPayment);
        paymentRepository.save(payment);
    }

    public void cashPayment(Integer cP) {
        CashPayment cashPayment = new CashPayment();
        cashPayment.setId(1);
        cashPayment.setCashAllowed(cP);
        cashPaymentRepository.save(cashPayment);
    }

    public void addAdminUsers(Long chatId) {
        AdminUsers adminUsers = new AdminUsers();
        adminUsers.setChatId(chatId);
        adminUsersRepository.save(adminUsers);
    }

    @Transactional
    public void newUserBarrier(Long chatId, String name, Integer place, Integer amountOfDays,
                               LocalDateTime dataTimeLastPayment, LocalDateTime dataTimeNextPayment) {
        User user = new User();
        UserBarrier userBarrier = new UserBarrier();

        user.setChatId(chatId);
        userBarrier.setChatId(chatId);
        userBarrier.setName(name);
        userBarrier.setParkingPlace(place);
        userBarrier.setStoppedBy(0);
        userBarrier.setAmountOfDays(amountOfDays);
        userBarrier.setDateTimeLastPayment(dataTimeLastPayment);
        userBarrier.setDateTimeNextPayment(dataTimeNextPayment);
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

        int numberOfPlaces = Integer.parseInt("numberOfPlaces");

        for (int i = 0; i < numberOfPlaces; i++) {
            User user = new User();
            UserBarrier userBarrier = new UserBarrier();

            user.setChatId(1392677678 + i);

            userBarrier.setChatId(1392677678 + i);

            setParkingPlaceLocal = true;
            while (setParkingPlaceLocal != false) {
                placeRandom = (int) ((Math.random() * (numberOfPlaces)) + 1);
                setParkingPlaceLocal = states.contains(placeRandom);
            }
            states.add(placeRandom);


            userBarrier.setParkingPlace(placeRandom);

            user.setUserBarrier(userBarrier);
            userBarrier.setUser(user);

            userRepository.save(user);
        }
    }

}
