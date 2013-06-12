package de.zalando.data.jpa.domain;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.google.common.base.Objects;

import de.zalando.data.annotation.BusinessKey;

@Entity
@Table(name = "purchase_order")
public class PurchaseOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "purchase_order_id_seq")
    private Integer id;

    @BusinessKey("UUID")
    private String businessKey;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PurchaseOrderPosition> positions = newArrayList();

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "po_invoice_address_id")
    private InvoiceAddress invoiceAddress;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "purchase_order_invoice_address", joinColumns = @JoinColumn(name = "poia_purchase_order_id"),
        inverseJoinColumns = @JoinColumn(name = "poia_invoice_address_id")
    )
    private List<InvoiceAddress> allInvoiceAddresses;

    public String getBusinessKey() {
        return businessKey;
    }

    public List<PurchaseOrderPosition> getPositions() {
        return positions;
    }

    public void setPositions(final List<PurchaseOrderPosition> positions) {
        this.positions = positions;
    }

    public InvoiceAddress getInvoiceAddress() {
        return invoiceAddress;
    }

    public void setInvoiceAddress(final InvoiceAddress invoiceAddress) {
        this.invoiceAddress = invoiceAddress;
    }

    public List<InvoiceAddress> getAllInvoiceAddresses() {
        if (allInvoiceAddresses == null) {
            allInvoiceAddresses = newArrayList();
        }

        return allInvoiceAddresses;
    }

    public void setAllInvoiceAddresses(final List<InvoiceAddress> allInvoiceAddresses) {
        this.allInvoiceAddresses = allInvoiceAddresses;
    }

    public Integer getId() {
        return id;
    }

    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("businesskey", getBusinessKey()).toString();
    }

}