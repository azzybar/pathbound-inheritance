// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

module dev.azzybar.pathbound {
    requires javafx.controls;
    requires javafx.fxml;

    opens dev.azzybar.pathbound.ui to javafx.fxml;
    exports dev.azzybar.pathbound;
}
