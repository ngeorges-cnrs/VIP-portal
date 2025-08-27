package fr.insalyon.creatis.vip.api.controller.admin;

import fr.insalyon.creatis.vip.api.controller.ApiController;
import fr.insalyon.creatis.vip.api.exception.ApiException;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.core.server.business.ConfigurationBusiness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.function.Supplier;

@RestController
@RequestMapping("admin/logout")
public class LogoutController extends ApiController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConfigurationBusiness configurationBusiness;
    private final Supplier<User> currentUserProvider;

    @Autowired
    protected LogoutController(Supplier<User> currentUserProvider, ConfigurationBusiness configurationBusiness) {
        this.configurationBusiness = configurationBusiness;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public void createAppVersion() throws ApiException {
        try {
            configurationBusiness.signout(currentUserProvider.get().getEmail());
        } catch (BusinessException e) {
            throw new ApiException(e);
        }
    }
}
