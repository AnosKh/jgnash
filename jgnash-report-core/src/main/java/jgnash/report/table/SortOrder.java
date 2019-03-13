/*
 * jGnash, a personal finance application
 * Copyright (C) 2001-2019 Craig Cavanaugh
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
package jgnash.report.table;

import jgnash.resource.util.ResourceUtils;

/**
 * Sort order enumeration
 *
 * @author Craig Cavanaugh
 */
public enum SortOrder {
    BY_NAME(ResourceUtils.getString("SortOrder.AccountName")),
    BY_BALANCE(ResourceUtils.getString("SortOrder.AccountBalanceDesc"));

    private final transient String description;

    SortOrder(final String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}