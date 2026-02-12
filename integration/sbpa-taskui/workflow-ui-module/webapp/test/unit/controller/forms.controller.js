/*global QUnit*/

sap.ui.define([
	"formssbpaui/workflow-ui-module/controller/forms.controller"
], function (Controller) {
	"use strict";

	QUnit.module("forms Controller");

	QUnit.test("I should test the forms controller", function (assert) {
		var oAppController = new Controller();
		oAppController.onInit();
		assert.ok(oAppController);
	});

});
