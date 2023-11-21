package ru.barrier.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import ru.barrier.services.DataBaseService;

import javax.validation.Valid;
import java.util.Collection;


@Controller
@RequestMapping("")
public class RedirectUrlPayment {
    @Autowired
    private DataBaseService dataBaseService;

    @GetMapping("/{idPayment}/{chatId}")
    public String redirectUrlPayment(@PathVariable("idPayment") String idPayment, @PathVariable("chatId") String chatId, Model model) {
        model.addAttribute("id", idPayment);
        model.addAttribute("chatId", chatId);
//        System.out.println(Long.parseLong(chatId));
        if (dataBaseService.getUserBarrierById(Long.parseLong(chatId)) != null) {
            return "redirect:https://yoomoney.ru/checkout/payments/v2/contract?orderId=" + idPayment;
        }
        return "/nopayment";
    }
}
