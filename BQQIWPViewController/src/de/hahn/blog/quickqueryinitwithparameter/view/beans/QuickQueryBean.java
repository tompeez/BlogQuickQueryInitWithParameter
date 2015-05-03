package de.hahn.blog.quickqueryinitwithparameter.view.beans;

import java.util.Map;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;

import javax.faces.context.FacesContext;

import oracle.adf.view.rich.component.rich.RichQuickQuery;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.QueryEvent;
import oracle.adf.view.rich.model.AttributeCriterion;
import oracle.adf.view.rich.model.AttributeDescriptor;
import oracle.adf.view.rich.model.FilterableQueryDescriptor;
import oracle.adf.view.rich.model.QueryModel;

import org.apache.myfaces.trinidad.util.ComponentReference;

public class QuickQueryBean {
    boolean needInit = true;
    private String dummy;
    private ComponentReference quickQuery;

    /**
     * Setter for Dummy property.
     * @param dummy
     */
    public void setDummy(String dummy) {
        this.dummy = dummy;
    }

    /**
     * getter for a string value names dummy in EL.
     * @return value of the dummy property
     */
    public String getDummy() {
        if (needInit) {
            needInit = false;
            initQuickQuery();
        }
        return "Search";
    }

    /**
     * Initialize the quickQuery component if a parameter tpCityName is found in the pageFlowScope. Once this is done, the pageFlowScope
     * variable tpCityName is set to null or removed.
     */
    public void initQuickQuery() {
        // get the PageFlowScope Params
        AdfFacesContext adfFacesCtx = AdfFacesContext.getCurrentInstance();
        Map<String, Object> scopePageFlowScopeVar = adfFacesCtx.getPageFlowScope();
        String paramCity = (String) scopePageFlowScopeVar.get("tpCityName");
        if (paramCity != null && !paramCity.isEmpty()) {
            // get query descriptor (the components value property)
            FilterableQueryDescriptor queryDescriptor = (FilterableQueryDescriptor) getQuickQuery().getValue();
            // get the current selected criterion (which should set in the ImplicitViewCriteriaQuery in hte pageDef
            AttributeCriterion attributeCriterion = queryDescriptor.getCurrentCriterion();
            // get the attribute name and check if it'S 'City'
            AttributeDescriptor attribute = attributeCriterion.getAttribute();
            String name = attribute.getName();
            // only set parameter if hte attribute matches the parameter
            if ("City".equalsIgnoreCase(name)) {
                attributeCriterion.setValue(paramCity);
                // remove value to allow new one in component
                scopePageFlowScopeVar.put("tpCityName", null);
                // set the parameter tothe attributeCriterion
                QueryModel model = getQuickQuery().getModel();
                model.setCurrentDescriptor(queryDescriptor);
                // create a queryEvent and invoke it
                QueryEvent qe = new QueryEvent(getQuickQuery(), queryDescriptor);
                invokeMethodExpression("#{bindings.ImplicitViewCriteriaQuery.processQuery}", Object.class, QueryEvent.class, qe);
            }
        }
    }


    private Object invokeMethodExpression(String expr, Class returnType, Class[] argTypes, Object[] args) {

        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext elctx = fc.getELContext();
        ExpressionFactory elFactory = fc.getApplication().getExpressionFactory();
        MethodExpression methodExpr = elFactory.createMethodExpression(elctx, expr, returnType, argTypes);
        return methodExpr.invoke(elctx, args);
    }

    private Object invokeMethodExpression(String expr, Class returnType, Class argType, Object argument) {
        return invokeMethodExpression(expr, returnType, new Class[] { argType }, new Object[] { argument });
    }

    /**
     * setter for component to ComponentReference.
     * @param quickQuery the component
     */
    public void setQuickQuery(RichQuickQuery quickQuery) {
        this.quickQuery = ComponentReference.newUIComponentReference(quickQuery);
    }

    /**
     * getter for the component from the component reference.
     * @return
     */
    public RichQuickQuery getQuickQuery() {
        if (quickQuery != null) {
            return (RichQuickQuery) quickQuery.getComponent();
        }
        return null;
    }
}
