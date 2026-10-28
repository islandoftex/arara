// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

import org.islandoftex.arara.api.ui.DialogRenderer

/** Dialog capability installed by the host, independent of rule-helper initialization. */
object Dialogs {
    /** Dialog renderer supplied by the host; the default rejects dialogs in headless use. */
    var renderer: DialogRenderer = HeadlessDialogRenderer
}
