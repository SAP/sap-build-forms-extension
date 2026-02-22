sap.ui.define(
  ["sap/ui/core/mvc/Controller", "sap/m/FlexItemData"],
  function (BaseController) {
    "use strict";

    return BaseController.extend(
      "forms.sbpa.taskui.workflowuimodule.controller.App",
      {
        onInit() {
          var isConsoleLog = false;

          var theView = this.getView();
          var contextModel = theView.getModel("context");
          contextModel.dataLoaded().then(function () {
            var formsScenarioBaseUrl =
              contextModel.getData().formsScenarioBaseUrl;
            var taskInstanceId = theView.getModel("task").getData().InstanceID;
            var url = undefined;
            if (formsScenarioBaseUrl && taskInstanceId) {
              url = `${formsScenarioBaseUrl}/task/${taskInstanceId}`;
            }
            if (url) {
              var oDynamicContentBox = theView.byId("dynamicContentBox");
              var embedTask = false;

              if (embedTask) {
                oDynamicContentBox.setHeight("100%");
                oDynamicContentBox.setWidth("100%");
                var oHTML = new sap.ui.core.HTML();
                var oLayoutData = oHTML.getLayoutData();
                if (oLayoutData) {
                  oLayoutData.setMinHeight("100%");
                  oLayoutData.setMinWidth("100%");
                  oLayoutData.setHeight("100%");
                  oLayoutData.setWidth("100%");
                } else {
                  oHTML.setLayoutData(
                    new sap.m.FlexItemData({
                      minHeight: "100%",
                      minWidth: "100%",
                      height: "100%",
                      width: "100%",
                    }),
                  );
                }
                oHTML.setContent(
                  `<iframe id="taskIframe" width="100%" height="100%" src="${url}?embedded=sbpaInbox" frameborder="0"></iframe>`,
                );
                oDynamicContentBox.addItem(oHTML);
                /*formsIframeContainer.attachAfterRendering(function (oEvent) {
                if (isConsoleLog) console.log("onAfterRendering is triggered");
                const taskIframe = document.getElementById("taskIframe");
                if (taskIframe) {*/
                var sectionElement = document.querySelector(
                  'section[id*="--mainPage-cont"]',
                );
                if (sectionElement) {
                  if (isConsoleLog)
                    console.log(
                      "Successfully found the section element:",
                      sectionElement,
                    );
                  // create a MutationObserver
                  const observer = new MutationObserver((mutations) => {
                    mutations.forEach((mutation) => {
                      // this is for the SBPA inbox in list mode
                      if (isConsoleLog)
                        console.log("Mutation detected!", mutation);
                      if (isConsoleLog) console.log(sectionElement);
                      if (sectionElement.style.height !== "") {
                        sectionElement.style.height = "";
                      }
                    });
                  });
                  // configure the MutationObserver, in this case only attribute changes are relevant
                  const config = {
                    childList: false,
                    subtree: false,
                    attributes: true,
                  };
                  // assign the observer to the sectionElement
                  observer.observe(sectionElement, config);
                  // this is mostly for the SBPA inbox in expert mode
                  if (sectionElement.style.height !== "") {
                    sectionElement.style.height = "";
                  }
                } else {
                  if (isConsoleLog)
                    console.error("The section element was not found!");
                }
              } else {
                var oButton = new sap.m.Button({
                  text: "Open Task",
                  press: function () {
                    window.open(url, "_blank");
                  },
                });
                var oFlexBox = new sap.m.FlexBox({
                  items: [oButton],
                });
                oDynamicContentBox.addItem(oFlexBox);
              }
            } else {
              console.error(
                `Task iFrame for taskInstanceId ${taskInstanceId} could not be determined!`,
              );
            }
          });
        },

        // a backup operation to find the section element from a child element, e.g. from taskIframe
        searchSectionRecursivelyAboveFromRefElement: function (
          referenceElement,
        ) {
          var currentElement = referenceElement;
          while (currentElement) {
            // check if current element is a 'section'-element and the id contains '--mainPage-cont'
            if (
              currentElement.tagName.toLowerCase() === "section" &&
              currentElement.id.includes("--mainPage-cont")
            ) {
              return currentElement;
            }
            // get the next parent element
            currentElement = currentElement.parentElement;
            // abort criteria if no parent element exists
            if (!currentElement) {
              console.error(
                "Document root reached, no valid section element found!",
              );
            }
          }
          return;
        },

        // a backup operation to get InboxAPI from the component controller
        getInboxAPIOfComponentControllerFromEvent: function (oEvent) {
          var componentController = oEvent.oSource.oParent.oParent.oParent
            .getController()
            .getOwnerComponent();
          if (componentController) return componentController.getInboxAPI();
          return;
        },
      },
    );
  },
);
