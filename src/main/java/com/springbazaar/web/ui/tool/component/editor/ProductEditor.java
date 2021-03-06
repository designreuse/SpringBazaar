package com.springbazaar.web.ui.tool.component.editor;

import com.springbazaar.web.security.ContextAwarePolicyEnforcement;
import com.springbazaar.domain.Person;
import com.springbazaar.domain.Product;
import com.springbazaar.domain.User;
import com.springbazaar.service.PersonService;
import com.springbazaar.service.ProductService;
import com.springbazaar.web.ui.MainUI;
import com.springbazaar.web.ui.WelcomeUI;
import com.springbazaar.web.ui.tool.SharedTag;
import com.vaadin.annotations.Theme;
import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.converter.StringToBigDecimalConverter;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Calendar;

@SpringUI(path = ProductEditor.NAME)
@Theme("valo")
public class ProductEditor extends MainUI {
    public static final String NAME = "/productEditor";
    private final Binder<Product> productBeanValidationBinder = new BeanValidationBinder<>(Product.class);
    private User user;
    private TextField productCaption = new TextField("Caption");
    private TextArea productDescription = new TextArea("Description");
    private TextField startPrice = new TextField("Start price");
    private TextField imageUrl = new TextField("Image");
    private Button productButton = new Button("ADD");

    @Autowired
    public ProductEditor(ProductService productService,
                         PersonService personService,
                         ContextAwarePolicyEnforcement policy) {
        productCaption.setWidth(500, Unit.PIXELS);

        productBeanValidationBinder.forField(productCaption)
                .asRequired("Product caption should be filled")
                .withValidator(
                        new StringLengthValidator("Product caption must be between 5 and 50 characters",
                                5,
                                50)
                )
                .bind("caption");

        productDescription.setWidth(500, Unit.PIXELS);
        productDescription.setRows(10);
        productDescription.setMaxLength(500);
        productBeanValidationBinder.forField(productDescription)
                .bind("description");


        startPrice.setWidth(100, Unit.PIXELS);
        productBeanValidationBinder.forField(startPrice)
                .withConverter(new StringToBigDecimalConverter("Could not convert value"))
                .bind("price");

        Product selectedEditProduct =
                (Product) VaadinSession.getCurrent().getAttribute(SharedTag.EDIT_PRODUCT_TAG);
        if (selectedEditProduct != null) {
            productCaption.setValue(selectedEditProduct.getCaption());
            productDescription.setValue(selectedEditProduct.getDescription());
            startPrice.setValue(selectedEditProduct.getPrice().toString());
            productButton.setCaption("Update");
            productButton.setIcon(VaadinIcons.FILE_REFRESH);
            productButton.setEnabled(true);
        } else {
            productButton.setEnabled(false);
            productButton.setIcon(VaadinIcons.FILE_ADD);
        }

        productBeanValidationBinder.addStatusChangeListener(statusChangeEvent ->
                productButton.setEnabled(productBeanValidationBinder.isValid()));
        productButton.addClickListener((Button.ClickListener) clickEvent -> {
            user = getCurrentUser();
            if (user == null) return;
            Product productForSave;
            if (selectedEditProduct == null) {
                productForSave = getProduct(user);
            } else {
                productForSave = getProduct(selectedEditProduct);
            }
            Person person = personService.getById(user.getPerson().getId());
            productForSave.setPerson(person);
            policy.checkPermission(productForSave, "PERM_PRODUCT_CREATE");
            productService.saveOrUpdate(productForSave);
            getPage().setLocation(WelcomeUI.NAME);
        });

        VerticalLayout fields = new VerticalLayout(productCaption, productDescription, imageUrl, startPrice, productButton);
        fields.setSpacing(true);
        fields.setMargin(new MarginInfo(true, true, true, false));
        fields.setSizeUndefined();

        VerticalLayout uiLayout = new VerticalLayout(fields);
        uiLayout.setSizeFull();
        uiLayout.setComponentAlignment(fields, Alignment.TOP_LEFT);

        setContent(uiLayout);
    }

    private Product getProduct(Product editProduct) {
        editProduct.setCaption(productCaption.getValue());
        editProduct.setDescription(productDescription.getValue());
        editProduct.setPrice(new BigDecimal(startPrice.getValue()));
        editProduct.setCreatedWhen(Calendar.getInstance().getTime());
        editProduct.setCreatedBy(user.getId());
        return editProduct;
    }

    private Product getProduct(User user) {
        return new Product(productCaption.getValue(),
                productDescription.getValue(),
                new BigDecimal("".equals(startPrice.getValue()) ? "0" : startPrice.getValue()),
                imageUrl.getValue(),
                Calendar.getInstance().getTime(),
                user.getId());
    }
}
