# Setup

## Setup Local Development Environment and IDE

Hint: The recommendation for the IDE is Visual Studio Code

1. Install Chocolatey from <https://chocolatey.org/install>
2. Install NodeJS (64-bit Installation) from <https://nodejs.org/en/download> via Chocolatey
3. Install Cloud Foundry CLI from <https://help.sap.com/docs/btp/sap-business-technology-platform/download-and-install-cloud-foundry-command-line-interface> or <https://github.com/cloudfoundry/cli?tab=readme-ov-file#downloads> via Chocolatey, e.g. with `choco install cloudfoundry-cli`
4. Install the following Cloud Foundry CLI plugins in a new "Command prompt" or "PowerShell" window
   1. 'Multiapps' via `cf install-plugin multiapps`
   2. 'Copyenv' via `cf install-plugin copyenv`
   3. 'Html5-plugin' via `cf install-plugin html5-plugin`
   4. 'Service-management' via `cf install-plugin service-management`
5. Install VS Code from <https://code.visualstudio.com/download>

## Clone Repository

1. Connect to GitHub Enterprise of Forms <https://github.wdf.sap.corp/forms>
2. Clone the repository using <https://github.wdf.sap.corp/forms/forms-sbpa-taskui.git>

## SAP Cloud Foundry Setup in Business Technology Platform (BTP)

### Create a Cloud Foundry Space

1. Create a new Cloud Foundry space
2. Obtain information about the `API Endpoint`, `Org`, and `Space` from the BTP Cockpit under Subaccount overview

### Cloud Foundry Login

1. Open a new terminal in VS Code
2. Log in to Cloud Foundry `cf login`
3. Enter the `<API Endpoint>`
4. Select the `<Org>` and `<Space>`

## Build and Deploy Project

1. Open the root folder of the directory where the cloned repository is stored inside VS Code
2. Run `mbt build`
3. If the `mta_archives/forms-sbpa-taskui_<version>.mtar` file is missing repeat step 2
4. Deploy the project with `cf deploy mta_archives/forms-sbpa-taskui_<version>.mtar` into your target Cloud Foundry Space
