# DataWedgeBarcodeHighlightingSample

*This application is intended for demonstration purposes only.*

*It is provided as-is without guarantee or warranty and may be modified to suit individual needs.*

=========================================================

This sample demonstrate how to highlight barcodes using Datawedge API.

It will displays colored overlays on barcodes/qrcodes/... that contains the value entered in the edit text under the Red, Green, Blue fields in the GUI.

The priority of the rules detection is Red over Yellow over Blue.

The rules are "contains" wich means that an overlay of a specific color will be displayed if it finds a barcode/qrcode/datamatrix/... that "contains" the string that the user entered in the main view of the application under the color's name.

You can use the scan mode to fill the colors highlighting rules (focus on the edit box, do a "select all", then scan your barcode)

After changing a value, the user has to push the button "Highlight Imager/Camera" to update the rules in DataWedge.

The reporting mode works the same way except that the symbology is added as a rule.
After configuring the reporting mode to report the barcode you are looking at, push the button "Report Imager/Camera" to setup the profile.
Then push the scan button to start the report workflow.
Once the preview is started, you'll see your targeted object highlighted in the color you selected.
All the other objects will be seen with a green overlay.
If the requested object (qrcode, ean13,... with a specific content) is available and has an overlay with the user custom color, you can hit the scan button again to retrieve the content of the object you were looking at.

The user can revert to regular scanning anytime by hitting the button "Regular Scan".

=========================================================

See License.md for licensing.

=========================================================
