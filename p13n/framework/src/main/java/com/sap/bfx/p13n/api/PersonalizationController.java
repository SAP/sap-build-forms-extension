package com.sap.bfx.p13n.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sap.bfx.exception.BadRequestException;
import com.sap.bfx.exception.FormsCoreException;
import com.sap.bfx.exception.NotAuthorizedException;
import com.sap.bfx.exception.NotFoundException;
import com.sap.bfx.p13n.model.Personalization;
import com.sap.bfx.p13n.model.Value;
import com.sap.bfx.p13n.security.P13NGrantedAuthorities;
import com.sap.bfx.p13n.service.PersonalizationService;
import com.sap.bfx.security.SecurityService;
import com.sap.bfx.security.SecurityUtils;
import com.sap.bfx.security.User;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for handling personalization-related API requests.
 */
@RestController
@RequestMapping("api/v1/p13n")
@Slf4j
//@CrossOrigin(origins = "http://localhost:3000")
public class PersonalizationController {

    private final PersonalizationService service;
    private final SecurityService securityService;

    /**
     * Constructs a new PersonalizationController with the specified services.
     *
     * @param service         the PersonalizationService to handle personalization operations
     * @param securityService the SecurityService to handle security and authorization checks
     */
    @Autowired
    public PersonalizationController(final PersonalizationService service, final SecurityService securityService) {
        this.service = service;
        this.securityService = securityService;
    }

    //General Functionalities

    /**
     * Retrieves all personalizations, optionally filtered by user.
     *
     * @param user the username to filter personalizations (optional)
     * @return a collection of Personalization objects
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Personalization> findAll(@RequestParam(required = false) String user) {
        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, false,
                P13NGrantedAuthorities.SBFX_P13NDisplay, P13NGrantedAuthorities.SBFX_P13NEdit);

        return service.findAllPersonalizations(user);
    }

    /**
     * Retrieves a personalization by its unique identifier.
     *
     * @param id the unique identifier of the personalization
     * @return the Personalization object with the specified ID
     * @throws BadRequestException if the ID is null
     * @throws NotFoundException   if no personalization is found with the specified ID
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Personalization findById(@PathVariable UUID id) {
        if (null == id) {
            throw new BadRequestException("Missing id");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, true,
                P13NGrantedAuthorities.SBFX_P13NDisplay, P13NGrantedAuthorities.SBFX_P13NEdit);

        var resultOpt = service.findPersonalizationById(id);
        if (resultOpt.isEmpty()) {
            throw new NotFoundException("Cannot find personalization with id '" + id + "'");
        }
        return resultOpt.get();
    }

    /**
     * Retrieves all values for static personalizations based on the specified locale.
     *
     * @param locale the locale for which to retrieve values
     * @return a map containing the values for static personalizations
     * @throws NotFoundException  if no values are found for the specified locale
     * @throws FormsCoreException if an error occurs while loading values
     */
    @GetMapping(value = "/values", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Map<String, ?> findAllValues(@RequestParam Locale locale) {
        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, true,
                P13NGrantedAuthorities.SBFX_P13NDisplay, P13NGrantedAuthorities.SBFX_P13NEdit);

        try {
            return service.getValuesForStaticPersonalizations(locale);
        } catch (NullPointerException e) {
            throw new NotFoundException("Values could not found");
        } catch (Exception e) {
            throw new FormsCoreException("Values could not be loaded");
        }
    }

    /**
     * Retrieves all value keys, optionally filtered by a search term.
     *
     * @param search the search term to filter value keys (optional)
     * @return a collection of value keys
     */
    @GetMapping(value = "/values/keys", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<String> findAllValueKeys(@RequestParam(required = false) String search) {
        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, true,
                P13NGrantedAuthorities.SBFX_P13NDisplay, P13NGrantedAuthorities.SBFX_P13NEdit);

        return service.findValueKeys(search);
    }

    /**
     * Retrieves all values associated with a specific key.
     *
     * @param key the key for which to retrieve values
     * @return a collection of Value objects associated with the specified key
     * @throws BadRequestException if the key is blank or null
     */
    @GetMapping(value = "/values/keys/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Value> findAllValuesByKey(@PathVariable String key) {
        if (StringUtils.isBlank(key)) {
            throw new BadRequestException("Missing key");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, true,
                P13NGrantedAuthorities.SBFX_P13NDisplay, P13NGrantedAuthorities.SBFX_P13NEdit);

        return service.findValuesForKey(key);
    }

    /**
     * Retrieves all applications for which personalizations exist.
     *
     * @return a collection of application names
     */
    @GetMapping(value = "/apps", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<String> findAllApps() {
        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, true,
                P13NGrantedAuthorities.SBFX_P13NDisplay, P13NGrantedAuthorities.SBFX_P13NEdit);

        return service.findApps();
    }

    /**
     * Creates a new personalization.
     *
     * @param personalization the Personalization object to be created
     * @return the created Personalization object
     * @throws BadRequestException     if the personalization is null or has an invalid key
     * @throws ResponseStatusException if a personalization with the same key already exists
     */
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Personalization create(@Valid @RequestBody Personalization personalization) {
        if (null == personalization) {
            throw new BadRequestException("Missing personalization");
        }
        if (personalization.getKey().startsWith("_")) {
            throw new BadRequestException("Key must not begin with '_'.");
        }

        //TODO OB: Check on more?
        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, true,
                P13NGrantedAuthorities.SBFX_P13NEdit, P13NGrantedAuthorities.SBFX_P13NEnduser);

        if (service.isPersonalizationExistent(personalization)) {
            // TODO OB: Own exception?
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Personalization already exists.");
        }
        personalization.setId(UUID.randomUUID());
        service.addPersonalization(personalization);
        return personalization;
    }

    //User Functionalities

    /**
     * Retrieves all personalizations for a specific user.
     *
     * @param username the username for which to retrieve personalizations
     * @return a collection of Personalization objects associated with the specified user
     * @throws BadRequestException if the username is blank or null
     */
    @GetMapping(value = "/user/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Personalization> findPersonalizationsByUserNameForUser(@PathVariable String username) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, true,
                P13NGrantedAuthorities.SBFX_P13NEnduser);

        checkOnYourOwnUsername(securitySession.getUser(), username);
        return service.findPersonalizationsByUserForUser(username);
    }

    /**
     * Retrieves all personalizations for a specific user and application.
     *
     * @param username    the username for which to retrieve personalizations
     * @param application the application for which to retrieve personalizations
     * @return a collection of Personalization objects associated with the specified user and application
     * @throws BadRequestException if the username or application is blank or null
     */
    @GetMapping(value = "/user/{username}/{application}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Personalization> findPersonalizationsByUserNameAndApplicationForUser(
            @PathVariable String username, @PathVariable String application) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }
        if (StringUtils.isBlank(application)) {
            throw new BadRequestException("Missing application");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, true,
                P13NGrantedAuthorities.SBFX_P13NEnduser);

        checkOnYourOwnUsername(securitySession.getUser(), username);
        return service.findPersonalizationsByUserAndAppForUser(username, application);
    }

    /**
     * Updates multiple personalizations for a specific user.
     *
     * @param username         the username for which to update personalizations
     * @param personalizations an array of Personalization objects to be updated
     * @return a collection of updated Personalization objects associated with the specified user
     * @throws BadRequestException if the username is blank or null, or if personalizations are missing or contain null values
     */
    @PutMapping(value = "/user/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Personalization> updateMultipleUser(@PathVariable String username,
                                                          @Valid @RequestBody Personalization[] personalizations) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }
        if (null == personalizations) {
            throw new BadRequestException("Missing personalizations");
        }
        if (Arrays.stream(personalizations).anyMatch(Objects::isNull)) {
            throw new BadRequestException("Missing personalization");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, true,
                P13NGrantedAuthorities.SBFX_P13NEnduser);

        checkOnYourOwnUsername(securitySession.getUser(), username);
        for (var personalization : personalizations) {
            var resultOpt = service.findPersonalizationById(personalization.getId());
            if (resultOpt.isEmpty()) {
                //get last suitable personalization
                Personalization lastSuitablePersonalization =
                        service.findPersonalizationByKeyUserAndApp(personalization.getKey(), personalization.getUser(),
                                personalization.getApp());

                //if personalization is existent at different id --> continue
                if (lastSuitablePersonalization != null &&
                        Objects.equals(lastSuitablePersonalization.getKey(), personalization.getKey()) &&
                        Objects.equals(lastSuitablePersonalization.getUser(), personalization.getUser()) &&
                        Objects.equals(lastSuitablePersonalization.getApp(), personalization.getApp())) {
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
            } else if (resultOpt.get() != personalization && resultOpt.get().isEditable() &&
                    resultOpt.get().isVisible()) {
                service.updatePersonalizationUser(personalization);
            }
        }

        if (personalizations.length > 0) {
            return service.findPersonalizationsByUserAndAppForUser(username, personalizations[0].getApp());
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Deletes all personalizations for a specific user and application.
     *
     * @param username    the username for which to delete personalizations
     * @param application the application for which to delete personalizations
     * @throws BadRequestException if the username or application is blank or null, or if the user and application are both "_"
     * @throws NotFoundException   if no personalizations are found for the specified user
     */
    @DeleteMapping(value = "/user/{username}/{application}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteForUser(@PathVariable String username, @PathVariable String application) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }
        if (StringUtils.isBlank(application)) {
            throw new BadRequestException("Missing application");
        }
        if (username.equals("_") && application.equals("_")) {
            throw new BadRequestException("User '_' and application '_' cannot be deleted");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, true,
                P13NGrantedAuthorities.SBFX_P13NEnduser);

        checkOnYourOwnUsername(securitySession.getUser(), username);
        if (!username.equals("_") && service.findPersonalizationsByUserForAdmin(username).isEmpty()) {
            throw new NotFoundException("Cannot find user with username '" + username + "'");
        }

        service.deleteUserForUser(username, application);
    }

    //Admin Functionalities

    /**
     * Retrieves all users, optionally filtered by a search term.
     *
     * @param search the search term to filter users (optional)
     * @return a collection of usernames
     */
    @GetMapping(value = "/admin/user", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<String> findAllUser(@RequestParam(required = false) String search) {
        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, true,
                P13NGrantedAuthorities.SBFX_P13NDisplay, P13NGrantedAuthorities.SBFX_P13NEdit);

        return service.findUsers(search);
    }

    /**
     * Retrieves all personalizations for a specific user (admin functionality).
     *
     * @param username the username for which to retrieve personalizations
     * @return a collection of Personalization objects associated with the specified user
     * @throws BadRequestException if the username is blank or null
     */
    @GetMapping(value = "/admin/user/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Personalization> findPersonalizationsByUserName(@PathVariable String username) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, true,
                P13NGrantedAuthorities.SBFX_P13NDisplay, P13NGrantedAuthorities.SBFX_P13NEdit);

        return service.findPersonalizationsByUserForAdmin(username);
    }

    /**
     * Retrieves all personalizations for a specific user and application (admin functionality).
     *
     * @param username    the username for which to retrieve personalizations
     * @param application the application for which to retrieve personalizations
     * @return a collection of Personalization objects associated with the specified user and application
     * @throws BadRequestException if the username or application is blank or null
     */
    @GetMapping(value = "/admin/user/{username}/{application}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Personalization> findPersonalizationsByUserNameAndApplication(@PathVariable String username,
                                                                                    @PathVariable String application) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }
        if (StringUtils.isBlank(application)) {
            throw new BadRequestException("Missing application");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, true,
                P13NGrantedAuthorities.SBFX_P13NDisplay, P13NGrantedAuthorities.SBFX_P13NEdit);

        return service.findPersonalizationsByUserAndAppForAdmin(username, application);
    }

    /**
     * Updates multiple personalizations for a specific user (admin functionality).
     *
     * @param username         the username for which to update personalizations
     * @param personalizations an array of Personalization objects to be updated
     * @return a collection of updated Personalization objects associated with the specified user
     * @throws BadRequestException if the username is blank or null, or if personalizations are missing or contain null values
     */
    @PutMapping(value = "/admin/user/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Personalization> updateMultipleAdmin(@PathVariable String username,
                                                           @Valid @RequestBody Personalization[] personalizations) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }
        if (null == personalizations) {
            throw new BadRequestException("Missing personalizations");
        }
        if (Arrays.stream(personalizations).anyMatch(Objects::isNull)) {
            throw new BadRequestException("Missing personalization");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, true,
                P13NGrantedAuthorities.SBFX_P13NEdit);

        for (var personalization : personalizations) {
            var resultOpt = service.findPersonalizationById(personalization.getId());
            if (resultOpt.isEmpty()) {
                //get last suitable personalization
                Personalization lastSuitablePersonalization =
                        service.findPersonalizationByKeyUserAndApp(personalization.getKey(), personalization.getUser(),
                                personalization.getApp());
                if (lastSuitablePersonalization == null || (lastSuitablePersonalization.isEditable() &&
                        (lastSuitablePersonalization.isVisible() || !personalization.isVisible()))) {
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

    /**
     * Deletes all personalizations for a specific user (admin functionality).
     *
     * @param username the username for which to delete personalizations
     * @throws BadRequestException if the username is blank or null, or if the user is "_"
     * @throws NotFoundException   if no personalizations are found for the specified user
     */
    @DeleteMapping(value = "/admin/user/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByUsername(@PathVariable String username) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }
        if (username.equals("_")) {
            throw new BadRequestException("User '_' cannot be deleted");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, false,
                P13NGrantedAuthorities.SBFX_P13NEdit);

        var resultOpt = service.findPersonalizationsByUserForAdmin(username);
        if (resultOpt.isEmpty()) {
            throw new NotFoundException("Cannot find user with username '" + username + "'");
        }
        service.deleteUser(username);
    }

    /**
     * Deletes all personalizations for a specific user and application (admin functionality).
     *
     * @param username    the username for which to delete personalizations
     * @param application the application for which to delete personalizations
     * @param ids         an optional array of personalization IDs to delete (if provided, only those personalizations will be deleted)
     * @throws BadRequestException if the username or application is blank or null, or if the user and application are both "_"
     * @throws NotFoundException   if no personalizations are found for the specified user
     */
    @DeleteMapping(value = "/admin/user/{username}/{application}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteForAdmin(@PathVariable String username, @PathVariable String application,
                               @RequestParam(required = false) UUID[] ids) {
        if (StringUtils.isBlank(username)) {
            throw new BadRequestException("Missing username");
        }
        if (StringUtils.isBlank(application)) {
            throw new BadRequestException("Missing application");
        }
        if (username.equals("_") && application.equals("_")) {
            throw new BadRequestException("User '_' and application '_' cannot be deleted");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, true,
                P13NGrantedAuthorities.SBFX_P13NEdit);

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
                if (!Objects.equals(p_present.getUser(), username) ||
                        !Objects.equals(p_present.getApp(), application)) {
                    throw new BadRequestException(
                            "Personalization with id " + id + " does not match provided username and app");
                }
            }
            for (var id : ids) {
                service.deletePersonalizationById(id);
            }
        }
    }

    /**
     * Updates values for a specific value name (admin functionality).
     *
     * @param value_name the name of the value to update
     * @param values     an ArrayList of Value objects to be updated
     * @return a collection of updated Value objects associated with the specified value name
     * @throws BadRequestException if the value_name is blank or null, or if values are missing or contain null values
     */
    @PutMapping(value = "/admin/values/{value_name}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Value> updateValues(@PathVariable String value_name,
                                          @Valid @RequestBody ArrayList<Value> values) {
        if (StringUtils.isBlank(value_name)) {
            throw new BadRequestException("Missing value_name");
        }
        if (null == values) {
            throw new BadRequestException("Missing values");
        }
        if (values.stream().anyMatch(Objects::isNull)) {
            throw new BadRequestException("Missing value");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, Boolean.TRUE,
                P13NGrantedAuthorities.SBFX_P13NEdit);

        //test for consistency
        if (!values.isEmpty()) {
            Set<String> keys = values.get(0).getValues().stream().map(str -> {
                if (str.contains("(") && str.contains(")")) {
                    return str.substring(str.indexOf("(") + 1, str.indexOf(")"));
                } else {
                    return "";
                }
            }).collect(Collectors.toSet());

            //all values changed must be of same key
            if (!values.stream().allMatch(x -> x.getId().equals(value_name))) {
                throw new BadRequestException("Values not valid");
            }

            for (var value : values) {
                if (value.getLocale() == null || value.getLocale().toString().equals("")) {
                    value.setLocale(new Locale("_"));
                }

                //check that each locale has same number of key entries
                Set<String> newKeys = value.getValues().stream().map(str -> {
                    if (str.contains("(") && str.contains(")")) {
                        return str.substring(str.indexOf("(") + 1, str.indexOf(")"));
                    } else {
                        return "";
                    }
                }).collect(Collectors.toSet());
                if (!Objects.equals(keys, newKeys)) {
                    throw new BadRequestException("Values not valid");
                }

                //check that String either has key or no '(' or ')'
                var isInvalid = value.getValues().stream().anyMatch(str -> {
                    if (str.contains("(") && str.contains(")")) {
                        return !(str.chars().filter(ch -> ch == '(').count() == 1 &&
                                str.chars().filter(ch -> ch == ')').count() == 1);
                    } else {
                        return str.contains("(") || str.contains(")");
                    }
                });
                if (isInvalid) {
                    throw new BadRequestException("Values not valid");
                }
            }

            //delete locales
            Set<Locale> differentLocales = service.findValuesForKey(value_name).stream().map(Value::getLocale)
                                                  .filter(locale -> values.stream().noneMatch(
                                                          val -> val.getLocale().equals(locale)))
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
                        Optional<Value> existing_value =
                                service.findValueByKeyAndLocale(value.getId(), value.getLocale());
                        if (existing_value.isPresent()) {
                            if (!existing_value.get().getValues().equals(value.getValues())) {
                                service.updateValue(value);
                            }
                        } else if (service.findValueByKeyAndLocale(value.getId(), new Locale("_")).isPresent() ||
                                value.getLocale().equals(new Locale("_"))) {
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

    /**
     * Checks if the username in the request matches the username in the authentication token.
     * If they do not match, a NotAuthorizedException is thrown.
     *
     * @param user            the authenticated user from the security session
     * @param requestUsername the username provided in the request path
     * @throws NotAuthorizedException if the usernames do not match
     */
    private void checkOnYourOwnUsername(User user, String requestUsername) {
        String tokenUsername = user.getId();
        if (!requestUsername.matches(tokenUsername)) {
            throw new NotAuthorizedException("personalization", "Not authorized to modify users other than your own",
                    tokenUsername);
        }
    }
}