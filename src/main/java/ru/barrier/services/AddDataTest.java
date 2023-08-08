package ru.barrier.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.barrier.models.User;
import ru.barrier.models.UserBarrier;
import ru.barrier.repository.UserBarrierRepository;
import ru.barrier.repository.UserRepository;
import java.util.TreeSet;

@Component
public class AddDataTest {
    @Autowired
    private UserRepository userRepository;

    public void newUser() {
        TreeSet states = new TreeSet<Integer>();
        int d = 1;
        boolean e = states.contains(d);

        boolean setParkingPlaceLocal = false;
        int placeRandom = 0;

        for (int i = 0; i < 11; i++) {
            User user = new User();
            UserBarrier userBarrier = new UserBarrier();

            user.setChatId(1292677678 + i);

            userBarrier.setChatId(1292677678 + i);

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
