sap.ui.define(
  [
    "sap/ui/core/UIComponent",
    "sap/ui/Device",
    "forms/sbpa/taskui/workflowuimodule/model/models",
  ],
  function (UIComponent, Device, models) {
    "use strict";

    return UIComponent.extend("forms.sbpa.taskui.workflowuimodule.Component", {
      metadata: {
        manifest: "json",
      },

      /**
       * The component is initialized by UI5 automatically during the startup of the app and calls the init method once.
       * @public
       * @override
       */
      init: function () {
        // call the base component's init function
        UIComponent.prototype.init.apply(this, arguments);

        // enable routing
        this.getRouter().initialize();

        // set the device model
        this.setModel(models.createDeviceModel(), "device");

        // set the task models (existing function call)
        this.setTaskModels();

        var isShowInboxFooter = false;
        
        if (!isShowInboxFooter) {
          // disable inbox footer regarding buggy and ugly functionality
          this.getInboxAPI().setShowFooter(false);
        }
      },

      setTaskModels: function () {
        // set the task model
        var startupParameters = this.getComponentData().startupParameters;
        this.setModel(startupParameters.taskModel, "task");

        // set the task context model
        var taskContextModel = new sap.ui.model.json.JSONModel(
          this._getTaskInstancesBaseURL() + "/context"
        );
        this.setModel(taskContextModel, "context");
      },

      _getTaskInstancesBaseURL: function () {
        return (
          this._getWorkflowRuntimeBaseURL() +
          "/task-instances/" +
          this.getTaskInstanceID()
        );
      },

      _getWorkflowRuntimeBaseURL: function () {
        var ui5CloudService = this.getManifestEntry(
          "/sap.cloud/service"
        ).replaceAll(".", "");
        var ui5ApplicationName = this.getManifestEntry(
          "/sap.app/id"
        ).replaceAll(".", "");
        var appPath = `${ui5CloudService}.${ui5ApplicationName}`;
        return `/${appPath}/api/public/workflow/rest/v1`;
      },

      getTaskInstanceID: function () {
        return this.getModel("task").getData().InstanceID;
      },

      getInboxAPI: function () {
        var startupParameters = this.getComponentData().startupParameters;
        return startupParameters.inboxAPI;
      },

      _fetchToken: function () {
        var fetchedToken;

        jQuery.ajax({
          url: this._getWorkflowRuntimeBaseURL() + "/xsrf-token",
          method: "GET",
          async: false,
          headers: {
            "X-CSRF-Token": "Fetch",
          },
          success(result, xhr, data) {
            fetchedToken = data.getResponseHeader("X-CSRF-Token");
          },
        });
        return fetchedToken;
      },

      _refreshTaskList: function () {
        this.getInboxAPI().updateTask("NA", this.getTaskInstanceID());
      },
    });
  }
);
