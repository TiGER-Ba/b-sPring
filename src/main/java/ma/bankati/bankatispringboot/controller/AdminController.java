package ma.bankati.bankatispringboot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.bankati.bankatispringboot.entity.User;
import ma.bankati.bankatispringboot.service.CreditService;
import ma.bankati.bankatispringboot.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final CreditService creditService;

    @GetMapping({"/", "/home"})
    public String adminHome(@AuthenticationPrincipal User user, Model model) {
        log.debug("Accès dashboard admin pour: {}", user.getUsername());

        long totalUsers = userService.countByRole(ma.bankati.bankatispringboot.entity.enums.ERole.USER);
        long pendingCredits = creditService.countByStatus(ma.bankati.bankatispringboot.entity.enums.CreditStatus.PENDING);

        model.addAttribute("user", user);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("pendingCredits", pendingCredits);
        // AppName et currentPage sont ajoutés automatiquement par GlobalControllerAdvice

        return "admin/home";
    }
}