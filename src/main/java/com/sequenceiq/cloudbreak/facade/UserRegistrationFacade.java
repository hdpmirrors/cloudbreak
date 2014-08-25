package com.sequenceiq.cloudbreak.facade;

import com.sequenceiq.cloudbreak.controller.json.InviteConfirmationRequest;
import com.sequenceiq.cloudbreak.controller.json.UserJson;

public interface UserRegistrationFacade extends CloudBreakFacade {

    UserJson registerUser(UserJson userJson);

    UserJson confirmInvite(String inviteToken, InviteConfirmationRequest inviteConfirmationRequest);

}
