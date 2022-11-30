# hackathon-23-3
Repository for the 23.3 hackathon


### Repository Contents
1. `main` branch, empty branch with this README
2. [`flow-demo`](tree/flow-demo) branch, a Vaadin 23.3 App generated in http://start.vaadin.com
- It has 4 views with different access permissions: `/hello` (public), `/about` (public), `/spreadsheet` (admin), `/master-detail` (admin), `/chat` (user), 
- Data: Two users for login (user:user and admin:admin), and 100 people for the master-detail view
- run it by typing `mvn`
3. [`hilla-demo`](tree/hilla-demo) branch, a Hilla 1.3 App generated in http://start.vaadin.com
- 2 views: `/hello` (needs login), `/master-detail` (needs admin)
- Data: Two users for login (user:user and admin:admin), and 100 people for the master-detail view
- run it by typing `mvn`

### Committing your work
Create a branch with your name and push to this repo.

### Showing your work

Write a summary of the main characteristics or your app, it could be in the README.md of your branch, or if you prefer a slide. Screenshots in the summary would be nice.

## V23.3 features

The list of main features are:

- Allow setting content of the Html component
- Rerouting to external URL
- Spreadsheet for Flow
- TabSheet component
- Tooltip component
- Chain validation in inputs
- DatePicker 2-digit year parsing
- Shift-click for Grid multi-sort
- React Hilla
- Swing Kit
- Kubernetes Kit
- Azure Kit
- SSO Kit
- Observability Kit

### What to do

The idea is to use some of the new features in V23.3, but there are many features for trying all at once, so you have to select some idea from the following list, or trye yours own.

Note that you can mix some of the ideas bellow, for instance complete the Spreadsheet challenge and then deploy your app in Kubernetes

#### Spreadsheet

Checkout the branch flow-demo, and modify the database so as we store customer credits.

When you select a credit, details can be displayed in a Spreadsheet, you can take advantage of an already existing amortisation excel sheet  and load it.

User can change amount, interest rate and time, in the spreadsheet, and they are saved in the credits table.

The credit data could be bound to a chart that is displayed in a different page container, note that spreadsheet for flow does not support embedded charts.

__Tip__: you could configure stateless login, so as you have not to relogin 


#### React-Hila

Checkout the hilla-demo and convert all the frontend code to use hilla-react instead of lit-element.

__Tip__: if you want to see how a hilla-react looks like you might generate a reference project with `npx @hilla/cli init --preset hilla-react hillou --pre`

#### Kubernetes Kit

Prepare two versions of the java demo application, e.g change theme colors or add some extra view.

If you have an azure or a google cluster you could deploy the application on them, otherwise you could try kubernetes in your local Docker Desktop.

Once deployed the two versions of the application, you should be able to migrate users from the blue to green app, and viceversa, and they should be notified and should not sufer any disruption in the service. 

__Tip__: configure preserve-on-refresh, for keeping the current work when reloading the view, it should 

[Kubernetes Kit documentation](https://vaadin.com/docs/latest/tools/kubernetes)

#### Single Sign-on (SSO) Kit

Checkout the flow-demo branch, replace authentication with SSO Kit configured for an external provider, for instance you might create a trial account in https://www.okta.com/ or try out azure

[SSO Kit documentation](https://vaadin.com/docs/latest/tools/sso)

#### Observability Kit

Prepare the flow-demo branch with the Observability Kit to export traces, install both Jaeger and Prometeus in you desktop, and check that you can see the traces and the metrics

[Observability Kit documentation](https://vaadin.com/docs/latest/tools/observability)

#### Azure Kit

Having an Azure subscription you might want to deploy the application in Azure Kubernetes by using terraform blueprints.

[Azure Kit documentation](https://vaadin.com/docs/latest/tools/azure)

#### Swing Kit

Checkout a Swing App from the internet, this [starter](https://github.com/nfriaa/swing-desktop-starter) might be a good option 

Checkout the flow branch in this repo, and prepare views and display them in the Swing App.

[Swing Kit documentation](https://vaadin.com/docs/latest/tools/swing)

## Hackaton Goals

The main reason of the hackathon is to be able to play with the new features in Vaadin by following documentation, and give feedback about any issues

## Tips

**Stateless authentication** It's possible to use stateless authentication in flow+spring by using the [hilla approach](https://hilla.dev/docs/security/spring-stateless)

**Installing Docker Desktop** Download and install from [here](https://www.docker.com/products/docker-desktop/)

**Setting up K8S clusters** Here you have some documents how to create your cluster in [Azure AKS](https://github.com/vaadin/k8s-blue-green/blob/main/SETUP-K8S-AKS.md) or [Google GKE](https://github.com/vaadin/k8s-blue-green/blob/main/SETUP-K8S-GKE.md)
