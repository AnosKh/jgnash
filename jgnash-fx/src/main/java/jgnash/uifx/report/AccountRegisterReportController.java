/*
 * jGnash, a personal finance application
 * Copyright (C) 2001-2016 Craig Cavanaugh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jgnash.uifx.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.prefs.Preferences;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

import jgnash.engine.Account;
import jgnash.engine.AccountType;
import jgnash.engine.CurrencyNode;
import jgnash.engine.Transaction;
import jgnash.engine.TransactionEntry;
import jgnash.engine.TransactionType;
import jgnash.ui.report.jasper.AbstractReportTableModel;
import jgnash.ui.report.jasper.ColumnHeaderStyle;
import jgnash.ui.report.jasper.ColumnStyle;
import jgnash.uifx.control.AccountComboBox;
import jgnash.uifx.control.DatePickerEx;
import jgnash.uifx.report.jasper.DynamicJasperReport;
import jgnash.uifx.views.register.RegisterFactory;
import jgnash.util.Nullable;
import jgnash.util.ResourceUtils;

import net.sf.jasperreports.engine.JasperPrint;

/**
 * Account Register Report
 *
 * @author Craig Cavanaugh
 */
public class AccountRegisterReportController extends DynamicJasperReport {

    @FXML
    AccountComboBox accountComboBox;

    @FXML
    private CheckBox showSplitsCheckBox;

    @FXML
    private DatePickerEx startDatePicker;

    @FXML
    private DatePickerEx endDatePicker;

    private static final String SHOW_SPLITS = "showSplits";

    @FXML
    private void initialize() {
        accountComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            handleRefresh();
        });

        showSplitsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            handleRefresh();
        });
    }

    private void handleRefresh() {
        final Preferences preferences = getPreferences();

        preferences.putBoolean(SHOW_SPLITS, showSplitsCheckBox.isSelected());

        if (refreshCallBackProperty().get() != null) {
            refreshCallBackProperty().get().run();
        }
    }

    private ReportModel createReportModel(final LocalDate startDate, final LocalDate endDate) {
        ReportModel model = new ReportModel();
        model.loadAccount(accountComboBox.getValue());

        return model;
    }

    @Override
    public JasperPrint createJasperPrint(boolean formatForCSV) {
        return createJasperPrint(createReportModel(startDatePicker.getValue(), endDatePicker.getValue()), formatForCSV);
    }

    @Override
    public String getReportName() {
        if (accountComboBox.getValue() != null) {
            return accountComboBox.getValue().getName();
        }
        return "";
    }

    @Override
    protected String getGrandTotalLegend() {
        return null;
    }

    @Override
    protected String getGroupFooterLabel() {
        return rb.getString("Word.Totals");
    }

    private class ReportModel extends AbstractReportTableModel {

        private boolean sumAmounts = false;

        final String split = ResourceUtils.getString("Button.Splits");

        Account account;

        final ObservableList<Row> rows = FXCollections.observableArrayList();

        final FilteredList<Row> filteredList = new FilteredList<>(rows);

        private final Predicate ALWAYS_TRUE = filteredList.getPredicate();

        String[] columnNames = RegisterFactory.getColumnNames(AccountType.BANK);

        ColumnStyle[] columnStyles = new ColumnStyle[] {ColumnStyle.SHORT_DATE, ColumnStyle.STRING, ColumnStyle.STRING,
                ColumnStyle.STRING, ColumnStyle.STRING, ColumnStyle.STRING, ColumnStyle.SHORT_AMOUNT,
                ColumnStyle.SHORT_AMOUNT, ColumnStyle.AMOUNT_SUM};

        void loadAccount(@Nullable final Account account) {
            this.sumAmounts = filteredList.getPredicate() != ALWAYS_TRUE;

            this.account = account;

            final boolean showSplits = showSplitsCheckBox.isSelected();

            if (account != null) {
                columnNames = RegisterFactory.getColumnNames(account.getAccountType());

                rows.clear();

                for (Transaction transaction : account.getSortedTransactionList()) {
                    if (showSplits && transaction.getTransactionType() == TransactionType.SPLITENTRY) {
                        List<TransactionEntry> transactionEntries = transaction.getTransactionEntries();
                        for (int i = 0; i < transactionEntries.size(); i++) {
                            rows.add(new Row(transaction, i));
                        }
                    } else {
                        rows.add(new Row(transaction, -1));
                    }
                }
            }
        }

        @Override
        public CurrencyNode getCurrency() {
            return accountComboBox.getValue().getCurrencyNode();
        }

        @Override
        public ColumnStyle getColumnStyle(int columnIndex) {
            if (sumAmounts && columnIndex == columnNames.length) {
                return ColumnStyle.GROUP_NO_HEADER;
            }

            return columnStyles[columnIndex];
        }

        @Override
        public ColumnHeaderStyle getColumnHeaderStyle(int columnIndex) {
            if (sumAmounts && columnIndex == columnNames.length) {
                return ColumnHeaderStyle.LEFT;
            }

            if (columnIndex < 6) {
                return ColumnHeaderStyle.LEFT;
            }

            return ColumnHeaderStyle.RIGHT;
        }

        @Override
        public int getRowCount() {
            return filteredList.size();
        }

        @Override
        public int getColumnCount() {
            if (sumAmounts) {
                return columnNames.length + 1;
            }
            return columnNames.length;
        }

        @Override
        public String getColumnName(final int columnIndex) {
            if (sumAmounts && columnIndex == getColumnCount()) {
                return "group";
            }
            return columnNames[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {

            if (columnIndex == getColumnCount()) { // group column
                return String.class;
            }

            switch (columnIndex) {
                case 0:
                    return LocalDate.class;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    return String.class;
                default:
                    return BigDecimal.class;
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return filteredList.get(rowIndex).getValueAt(columnIndex);
        }

        private class Row {
            private final Transaction transaction;
            private final BigDecimal amount;
            private final int signum;
            private TransactionEntry transactionEntry;


            Row(final Transaction transaction, final int entry) {
                this.transaction = transaction;
                amount = transaction.getAmount(account);
                signum = amount.signum();

                if (entry >= 0) {
                    transactionEntry = transaction.getTransactionEntries().get(entry);
                }
            }

            Object getValueAt(int columnIndex) {

                if (transactionEntry == null) {
                    switch (columnIndex) {
                        case 0:
                            return transaction.getLocalDate();
                        case 1:
                            return transaction.getNumber();
                        case 2:
                            return transaction.getPayee();
                        case 3:
                            return transaction.getMemo(account);
                        case 4:
                            if (transaction.getTransactionType() == TransactionType.SPLITENTRY) {
                                return "[ " + transaction.size() + " " + split + " ]";
                            } else {
                                final TransactionEntry entry = transaction.getTransactionEntries().get(0);

                                if (entry.getCreditAccount() != account) {
                                    return entry.getCreditAccount().getName();
                                }
                            }
                        case 5:
                            return transaction.getReconciled(account).toString();
                        case 6:
                            if (signum >= 0) {
                                return amount;
                            }
                            return null;
                        case 7:
                            if (signum < 0) {
                                return amount.abs();
                            }
                            return null;
                        case 8:
                            return account.getBalanceAt(transaction);
                        default:
                            return null;
                    }
                } else {
                    switch (columnIndex) {
                        case 4:
                            if (account != transactionEntry.getCreditAccount()) {
                                return transactionEntry.getCreditAccount().getName();
                            } else {
                                return transactionEntry.getDebitAccount().getName();
                            }
                        case 5:
                            return transaction.getReconciled(account).toString();
                        case 6:
                            if (signum >= 0) {
                                return amount;
                            }
                            return null;
                        case 7:
                            if (signum < 0) {
                                return amount.abs();
                            }
                            return null;
                        default:
                            return null;
                    }
                }
            }
        }
    }
}