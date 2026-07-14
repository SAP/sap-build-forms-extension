package com.sap.bfx.p13n.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sap.bfx.exception.BadRequestException;
import com.sap.bfx.exception.FormsCoreException;
import com.sap.bfx.exception.NotAuthorizedException;
import com.sap.bfx.exception.NotFoundException;
import com.sap.bfx.p13n.model.Personalization;
import com.sap.bfx.p13n.model.Value;
import com.sap.bfx.p13n.security.P13NGroups;
import com.sap.bfx.p13n.service.PersonalizationService;
import com.sap.bfx.security.SecurityService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/p13n")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class PersonalizationController {

    private final PersonalizationService service;
    private final SecurityService securityService;

    @Autowired
    public PersonalizationController(final PersonalizationService service, final SecurityService securityService) {
        this.service = service;
        this.securityService = securityService;
    }

    //General Functionalities

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Personalization> findAll(@RequestParam(required = false) String user, AbstractAuthenticationToken token) {
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NDisplay, P13NGroups.SBFX_P13NEdit);
        return service.findAllPersonalizations(user);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Personalization findById(@PathVariable UUID id, AbstractAuthenticationToken token) {
        if (null == id) {
            throw new BadRequestException("Missing id");
        }
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NDisplay, P13NGroups.SBFX_P13NEdit);
        var resultOpt = service.findPersonalizationById(id);
        if (resultOpt.isEmpty()) {
            throw new NotFoundException("Cannot find personalization with id '" + id + "'");
        }
        return resultOpt.get();
    }

    @GetMapping(value = "/values", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Map<String, ?> findAllValues(@RequestParam Locale locale, AbstractAuthenticationToken token) {
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NDisplay, P13NGroups.SBFX_P13NEdit);
        try {
            return service.getValuesForStaticPersonalizations(locale);
        } catch (NullPointerException e) {
            throw new NotFoundException("Values could not found");
        } catch (Exception e) {
            throw new FormsCoreException("Values could not be loaded");
        }
    }

    @GetMapping(value = "/values/keys", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<String> findAllValueKeys(@RequestParam(required = false) String search, AbstractAuthenticationToken token) {
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NDisplay, P13NGroups.SBFX_P13NEdit);
        return service.findValueKeys(search);
    }

    @GetMapping(value = "/values/keys/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Value> findAllValuesByKey(@PathVariable String key, AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(key)) {
            throw new BadRequestException("Missing key");
        }
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NDisplay, P13NGroups.SBFX_P13NEdit);
        return service.findValuesForKey(key);
    }

    @GetMapping(value = "/apps", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<String> findAllApps(AbstractAuthenticationToken token) {
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NDisplay, P13NGroups.SBFX_P13NEdit);
        return service.findApps();
    }

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Personalization create(@Valid @RequestBody Personalization personalization, AbstractAuthenticationToken token) {
        if (null == personalization) {
            throw new BadRequestException("Missing personalization");
        }
        if (personalization.getKey().startsWith("_")) {
            throw new BadRequestException("Key must not begin with '_'.");
        }
        //TODO OB: Check on more?
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NEdit, P13NGroups.SBFX_P13NEnduser);
        if (service.isPersonalizationExistent(personalization)) {
            // TODO OB: Own exception?
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Personalization already exists.");
        }
        personalization.setId(UUID.randomUUID());
        service.addPersonalization(personalization);
        return personalization;
    }

    //User Functionalities

    @GetMapping(value = "/user/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Personalization> findPersonalizationsByUserNameForUser(@PathVariable String username, AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }
        securityService.ensureAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NEnduser);
        checkOnYourOwnUsername(token, username);
        return service.findPersonalizationsByUserForUser(username);
    }

    @GetMapping(value = "/user/{username}/{application}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Personalization> findPersonalizationsByUserNameAndApplicationForUser(
            @PathVariable String username,
            @PathVariable String application,
            AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }
        if (StringUtils.isBlank(application)) {
            throw new BadRequestException("Missing application");
        }
        securityService.ensureAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NEnduser);
        checkOnYourOwnUsername(token, username);
        return service.findPersonalizationsByUserAndAppForUser(username, application);
    }

    @PutMapping(value = "/user/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Personalization> updateMultipleUser(@PathVariable String username,
                                                          @Valid @RequestBody Personalization[] personalizations,
                                                          AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }
        if (null == personalizations) {
            throw new BadRequestException("Missing personalizations");
        }
        if (Arrays.stream(personalizations).anyMatch(Objects::isNull)) {
            throw new BadRequestException("Missing personalization");
        }
        securityService.ensureAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NEnduser);
        checkOnYourOwnUsername(token, username);
        for (var personalization : personalizations) {
            var resultOpt = service.findPersonalizationById(personalization.getId());
            if (resultOpt.isEmpty()) {
                //get last suitable personalization
                Personalization lastSuitablePersonalization = service.findPersonalizationByKeyUserAndApp(
                        personalization.getKey(),
                        personalization.getUser(),
                        personalization.getApp());

                //if personalization is existent at different id --> continue
                if (lastSuitablePersonalization != null &&
                        Objects.equals(lastSuitablePersonalization.getKey(), personalization.getKey())
                        && Objects.equals(lastSuitablePersonalization.getUser(), personalization.getUser())
                        && Objects.equals(lastSuitablePersonalization.getApp(),
                        personalization.getApp())) {
                    continue;
                }
                //create personalization
                if (lastSuitablePersonalization == null ||
                        (lastSuitablePersonalization.isEditable() && lastSuitablePersonalization.isVisible())) {
                    if (lastSuitablePersonalization != null) {
                        personalization.setVisible(lastSuitablePersonalization.isVisible());
                        personalization.setEditable(lastSuitablePersonalization.isEditable());
                    } else {
                        personalization.setVisible(true);
                        personalization.setEditable(true);
                    }
                    personalization.setId(UUID.randomUUID());
                    service.addPersonalization(personalization);
                }
                // update personalization
            } else if (resultOpt.get() != personalization
                    && resultOpt.get().isEditable()
                    && resultOpt.get().isVisible()) {
                service.updatePersonalizationUser(personalization);
            }
        }

        if (personalizations.length > 0) {
            return service.findPersonalizationsByUserAndAppForUser(username, personalizations[0].getApp());
        } else {
            return new ArrayList<>();
        }
    }

    @DeleteMapping(value = "/user/{username}/{application}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteForUser(@PathVariable String username,
                              @PathVariable String application,
                              AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }
        if (StringUtils.isBlank(application)) {
            throw new BadRequestException("Missing application");
        }
        if (username.equals("_") && application.equals("_")) {
            throw new BadRequestException("User '_' and application '_' cannot be deleted");
        }
        securityService.ensureAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NEnduser);
        checkOnYourOwnUsername(token, username);
        if (!username.equals("_") && service.findPersonalizationsByUserForAdmin(username).isEmpty()) {
            throw new NotFoundException("Cannot find user with username '" + username + "'");
        }

        service.deleteUserForUser(username, application);
    }

    private String getTokenUsername(AbstractAuthenticationToken token) {
        return token.getName().substring((-1 != token.getName().lastIndexOf("/")) ? token.getName().lastIndexOf("/") + 1 : 0);
    }

    private void checkOnYourOwnUsername(AbstractAuthenticationToken token, String requestUsername) {
        String tokenUsername = getTokenUsername(token);
        if (!requestUsername.matches(tokenUsername)) {
            throw new NotAuthorizedException(null, new String[]{"Not authorized to modify users other than your own"}, tokenUsername);
        }
    }

    //Admin Functionalities

    @GetMapping(value = "/admin/user", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<String> findAllUser(@RequestParam(required = false) String search, AbstractAuthenticationToken token) {
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NDisplay, P13NGroups.SBFX_P13NEdit);
        return service.findUsers(search);
    }

    @GetMapping(value = "/admin/user/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Personalization> findPersonalizationsByUserName(@PathVariable String username, AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NDisplay, P13NGroups.SBFX_P13NEdit);
        return service.findPersonalizationsByUserForAdmin(username);
    }

    @GetMapping(value = "/admin/user/{username}/{application}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Personalization> findPersonalizationsByUserNameAndApplication(
            @PathVariable String username,
            @PathVariable String application,
            AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }
        if (StringUtils.isBlank(application)) {
            throw new BadRequestException("Missing application");
        }
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NDisplay, P13NGroups.SBFX_P13NEdit);
        return service.findPersonalizationsByUserAndAppForAdmin(username, application);
    }

    @PutMapping(value = "/admin/user/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Personalization> updateMultipleAdmin(@PathVariable String username,
                                                           @Valid @RequestBody Personalization[] personalizations,
                                                           AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }
        if (null == personalizations) {
            throw new BadRequestException("Missing personalizations");
        }
        if (Arrays.stream(personalizations).anyMatch(Objects::isNull)) {
            throw new BadRequestException("Missing personalization");
        }
        securityService.ensureAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NEdit);
        for (var personalization : personalizations) {
            var resultOpt = service.findPersonalizationById(personalization.getId());
            if (resultOpt.isEmpty()) {
                //get last suitable personalization
                Personalization lastSuitablePersonalization = service.findPersonalizationByKeyUserAndApp(
                        personalization.getKey(),
                        personalization.getUser(),
                        personalization.getApp());
                if (lastSuitablePersonalization == null || (lastSuitablePersonalization.isEditable()
                        && (lastSuitablePersonalization.isVisible() || !personalization.isVisible()))) {
                    personalization.setId(UUID.randomUUID());
                    service.addPersonalization(personalization);
                }
            } else if (resultOpt.get() != personalization) {
                service.updatePersonalizationAdmin(personalization);
            }
        }

        if (personalizations.length > 0) {
            return service.findPersonalizationsByUserAndAppForAdmin(username, personalizations[0].getApp());
        } else {
            return new ArrayList<>();
        }
    }

    @DeleteMapping(value = "/admin/user/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByUsername(@PathVariable String username, AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }
        if (username.equals("_")) {
            throw new BadRequestException("User '_' cannot be deleted");
        }
        securityService.ensureAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NEdit);
        var resultOpt = service.findPersonalizationsByUserForAdmin(username);
        if (resultOpt.isEmpty()) {
            throw new NotFoundException("Cannot find user with username '" + username + "'");
        }
        service.deleteUser(username);
    }

    @DeleteMapping(value = "/admin/user/{username}/{application}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteForAdmin(@PathVariable String username, @PathVariable String application, @RequestParam(required = false) UUID[] ids, AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }
        if (StringUtils.isBlank(application)) {
            throw new BadRequestException("Missing application");
        }
        if (username.equals("_") && application.equals("_")) {
            throw new BadRequestException("User '_' and application '_' cannot be deleted");
        }
        securityService.ensureAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NEdit);

        if (ids == null) {
            if (service.findPersonalizationsByUserForAdmin(username).isEmpty()) {
                throw new NotFoundException("Cannot find user with username '" + username + "'");
            }
            service.deleteUser(username, application);

        } else {
            for (var id : ids) {
                var p = service.findPersonalizationById(id);
                if (p.isEmpty()) {
                    throw new BadRequestException("Cannot find personalization with id " + id);
                }
                var p_present = p.get();
                if (p_present.getKey().startsWith("_")) {
                    throw new BadRequestException("Personalizations beginning with '_' cannot be deleted");
                }
                if (!Objects.equals(p_present.getUser(), username) || !Objects.equals(p_present.getApp(), application)) {
                    throw new BadRequestException("Personalization with id " + id + " does not match provided username and app");
                }
            }
            for (var id : ids) {
                service.deletePersonalizationById(id);
            }
        }
    }

    @PutMapping(value = "/admin/values/{value_name}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Value> updateValues(@PathVariable String value_name,
                                          @Valid @RequestBody ArrayList<Value> values,
                                          AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(value_name)) {
            throw new BadRequestException("Missing value_name");
        }
        if (null == values) {
            throw new BadRequestException("Missing values");
        }
        if (values.stream().anyMatch(Objects::isNull)) {
            throw new BadRequestException("Missing value");
        }
        securityService.ensureAuthorized(token, null, Boolean.TRUE, P13NGroups.SBFX_P13NEdit);
        //test for consistency
        if (!values.isEmpty()) {
            Set<String> keys = values
                    .get(0)
                    .getValues()
                    .stream()
                    .map(str -> {
                        if (str.contains("(") && str.contains(")")) {
                            return str.substring(str.indexOf("(") + 1, str.indexOf(")"));
                        } else {
                            return "";
                        }
                    })
                    .collect(Collectors.toSet());

            //all values changed must be of same key
            if (!values.stream().allMatch(x -> x.getId().equals(value_name))) {
                throw new BadRequestException("Values not valid");
            }

            for (var value : values) {
                if (value.getLocale() == null || value.getLocale().toString().equals("")) {
                    value.setLocale(new Locale("_"));
                }

                //check that each locale has same number of key entries
                Set<String> newKeys = value.getValues()
                        .stream()
                        .map(str -> {
                            if (str.contains("(") && str.contains(")")) {
                                return str.substring(str.indexOf("(") + 1, str.indexOf(")"));
                            } else {
                                return "";
                            }
                        })
                        .collect(Collectors.toSet());
                if (!Objects.equals(keys, newKeys)) {
                    throw new BadRequestException("Values not valid");
                }

                //check that String either has key or no '(' or ')'
                var isInvalid = value.getValues()
                        .stream()
                        .anyMatch(str -> {
                            if (str.contains("(") && str.contains(")")) {
                                return !(str.chars().filter(ch -> ch == '(').count() == 1
                                        && str.chars().filter(ch -> ch == ')').count() == 1);
                            } else {
                                return str.contains("(") || str.contains(")");
                            }
                        });
                if (isInvalid) {
                    throw new BadRequestException("Values not valid");
                }
            }

            //delete locales
            Set<Locale> differentLocales = service.findValuesForKey(value_name)
                    .stream()
                    .map(Value::getLocale)
                    .filter(locale -> values.stream().noneMatch(val -> val.getLocale().equals(locale)))
                    .collect(Collectors.toSet());
            for (Locale locale : differentLocales) {
                service.deleteValue(locale, value_name);
            }

            //update values
            for (var value : values) {
                try {
                    if (value.getValues().isEmpty()) {
                        service.deleteValue(value.getLocale(), value.getId());
                    } else {
                        Optional<Value> existing_value = service.findValueByKeyAndLocale(value.getId(), value.getLocale());
                        if (existing_value.isPresent()) {
                            if (!existing_value.get().getValues().equals(value.getValues())) {
                                service.updateValue(value);
                            }
                        } else if (service.findValueByKeyAndLocale(value.getId(), new Locale("_")).isPresent()
                                || value.getLocale().equals(new Locale("_"))) {
                            service.addValue(value);
                        } else {
                            throw new BadRequestException("Values not valid");
                        }
                    }
                } catch (JsonProcessingException e) {
                    throw new FormsCoreException("Value could not be changed");
                }
            }
            return service.findValuesForKey(values.get(0).getId());

        } else {
            return new ArrayList<>();
        }
    }
}