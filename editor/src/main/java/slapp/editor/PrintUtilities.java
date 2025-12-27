/*
Copyright (c) 2024 Tony Roy

This file is part of the Symbolic Logic APPlication (SLAPP).

SLAPP is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

SLAPP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with SLAPP.  If not, see
<https://www.gnu.org/licenses/>.
 */

package slapp.editor;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableSet;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import static javafx.scene.control.ButtonType.OK;
import static slapp.editor.EditorMain.mainStage;
import static slapp.editor.EditorMain.os;


import slapp.editor.main_window.MainWindowView;


/**
 * Utility items to facilitate printing
 */
public class PrintUtilities {
    private static PageLayout pageLayout;
    //this is the layout visible to the rest of the program for the printable area
    private static DoubleProperty pageWidth = new SimpleDoubleProperty();
    private static DoubleProperty pageHeight = new SimpleDoubleProperty();

    private static PageLayout internalPageLayout;
    private static double internalPrintHeight;
    // this is the layout with space for footer, for internal use


    private static Printer pdfPrinter = null;
    private static Printer printer = null;
    private static Region spacer = new Region();
    private static double baseScale = 1.0;
    private static List<PrintBufferItem> printBuffer = new ArrayList<>();
    private static VBox topBox;
    private static double minHorizOffset;
    private static double minVertOffset;


    /**
     * Set default print values
     */
    static {
        spacer.setVisible(false);
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            PageLayout baseLayout = job.getJobSettings().getPageLayout();
            pageHeight.set(baseLayout.getPrintableHeight());
            pageWidth.set(baseLayout.getPrintableWidth());
            printer = job.getPrinter();
            double bottomMargin = Math.max(baseLayout.getBottomMargin(), 48);
            pageLayout = printer.createPageLayout(baseLayout.getPaper(), baseLayout.getPageOrientation(), baseLayout.getLeftMargin(), baseLayout.getRightMargin(), baseLayout.getTopMargin(), bottomMargin );

            PageLayout minLayout = printer.createPageLayout(baseLayout.getPaper(), pageLayout.getPageOrientation(), Printer.MarginType.HARDWARE_MINIMUM);
            minHorizOffset = minLayout.getLeftMargin();
            minVertOffset = minLayout.getTopMargin();



            //In portrait margins appear to be set at Margin + Printer Minimum instead of Math.max(Margin, printerMinimum)
            //and then to truncate what is outside the margin.  I reduce left and top margins by the printer minimum
            // (so that the sum is the requested margin) and then translate the node forward (at printNodes)
            // by the printer minimum (so that the content appears at the "dummy" margin).

            //In landscape, top and bottom margins appear reversed. And in mac landscape margin or translate settings seem
            // to make a difference!  It's on the page, but not quite correctly positioned.

            //Really don't understand what is happening (JavaFX25) - get no real help from web.

            double bottom = 18.0;
            double leftMargin = pageLayout.getLeftMargin();
            double rightMargin = pageLayout.getRightMargin();
            double topMargin = pageLayout.getTopMargin();

            if (leftMargin > minHorizOffset) {
                leftMargin = leftMargin - minHorizOffset;
            }
            if (topMargin > minVertOffset) {
                topMargin = topMargin - minVertOffset;
            }
            if (pageLayout.getPageOrientation() == PageOrientation.LANDSCAPE) {
                internalPrintHeight = pageLayout.getPaper().getWidth() - pageLayout.getTopMargin() - 18.0;
                internalPageLayout = printer.createPageLayout(pageLayout.getPaper(), pageLayout.getPageOrientation(), leftMargin, rightMargin, bottom, topMargin);
            }
            else {
                internalPrintHeight = pageLayout.getPaper().getHeight() - pageLayout.getTopMargin() - 18.0;
                internalPageLayout = printer.createPageLayout(pageLayout.getPaper(), pageLayout.getPageOrientation(), leftMargin, rightMargin, topMargin, bottom);
            }

        }
        else {
            pageHeight.set(684); pageWidth.set(504);
        }
    }


    private static PrinterJob getPrinterJob() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            EditorAlerts.showSimpleAlert("Cannot find Printer", "I do not find an installed printer.  Please install printer and try again.");
        }
        else {
            if (printer == null) {
                initializePrintSetup(job);
            }
        }
        return job;
    }

    private static void initializePrintSetup(PrinterJob job) {
        PageLayout baseLayout = job.getJobSettings().getPageLayout();
        pageHeight.set(baseLayout.getPrintableHeight());
        pageWidth.set(baseLayout.getPrintableWidth());
        printer = job.getPrinter();
        double bottomM = Math.max(baseLayout.getBottomMargin(), 48);
        pageLayout = printer.createPageLayout(baseLayout.getPaper(), baseLayout.getPageOrientation(), baseLayout.getLeftMargin(), baseLayout.getRightMargin(), baseLayout.getTopMargin(), bottomM );

        PageLayout minLayout = printer.createPageLayout(baseLayout.getPaper(), pageLayout.getPageOrientation(), Printer.MarginType.HARDWARE_MINIMUM);
        minHorizOffset = minLayout.getLeftMargin();
        minVertOffset = minLayout.getTopMargin();

        double bottom = 18.0;
        double leftMargin = pageLayout.getLeftMargin();
        double rightMargin = pageLayout.getRightMargin();
        double topMargin = pageLayout.getTopMargin();

        if (leftMargin > minHorizOffset) {
            leftMargin = leftMargin - minHorizOffset;
        }
        if (topMargin > minVertOffset) {
            topMargin = topMargin - minVertOffset;
        }
        if (pageLayout.getPageOrientation() == PageOrientation.LANDSCAPE) {
            internalPrintHeight = pageLayout.getPaper().getWidth() - pageLayout.getTopMargin() - 18.0;
            internalPageLayout = printer.createPageLayout(pageLayout.getPaper(), pageLayout.getPageOrientation(), leftMargin, rightMargin, bottom, topMargin);
        }
        else {
            internalPrintHeight = pageLayout.getPaper().getHeight() - pageLayout.getTopMargin() - 18.0;
            internalPageLayout = printer.createPageLayout(pageLayout.getPaper(), pageLayout.getPageOrientation(), leftMargin, rightMargin, topMargin, bottom);
        }
    }

    /**
     * Update page layout settings by page setup dialog
     */
    public static void updatePageLayout() {
        PrinterJob job = getPrinterJob();
        if (job != null) {
            job.getJobSettings().setPageLayout(pageLayout);

           // boolean proceed = job.showPageSetupDialog(null);
            boolean proceed = job.showPageSetupDialog(mainStage);

            if (proceed) {
                PageLayout baseLayout = job.getJobSettings().getPageLayout();
                double bottomMargin = Math.max(baseLayout.getBottomMargin(), 48);
                pageLayout = printer.createPageLayout(baseLayout.getPaper(), baseLayout.getPageOrientation(), baseLayout.getLeftMargin(), baseLayout.getRightMargin(), baseLayout.getTopMargin(), bottomMargin );
                pageHeight.set(pageLayout.getPrintableHeight());
                pageWidth.set(pageLayout.getPrintableWidth());

                PageLayout minLayout = printer.createPageLayout(baseLayout.getPaper(), pageLayout.getPageOrientation(), Printer.MarginType.HARDWARE_MINIMUM);
                minHorizOffset = minLayout.getLeftMargin();
                minVertOffset = minLayout.getTopMargin();

                double bottom = 18.0;
                double leftMargin = pageLayout.getLeftMargin();
                double rightMargin = pageLayout.getRightMargin();
                double topMargin = pageLayout.getTopMargin();

                if (leftMargin > minHorizOffset) {
                    leftMargin = leftMargin - minHorizOffset;
                }
                if (topMargin > minVertOffset) {
                    topMargin = topMargin - minVertOffset;
                }
                if (pageLayout.getPageOrientation() == PageOrientation.LANDSCAPE) {
                    internalPrintHeight = pageLayout.getPaper().getWidth() - pageLayout.getTopMargin() - 18.0;
                    internalPageLayout = printer.createPageLayout(pageLayout.getPaper(), pageLayout.getPageOrientation(), leftMargin, rightMargin, bottom, topMargin);
                }
                else {
                    internalPrintHeight = pageLayout.getPaper().getHeight() - pageLayout.getTopMargin() - 18.0;
                    internalPageLayout = printer.createPageLayout(pageLayout.getPaper(), pageLayout.getPageOrientation(), leftMargin, rightMargin, topMargin, bottom);
                }

            }
            job.endJob();
        }
    }

    /**
     * Select export printer with failure message
     */
    public static void exportSetup() {
        if (!runExportSetup())  EditorAlerts.showSimpleAlert("Print Problem", "Export setup failed");
    }


    /**
     * Select export printer with print dialog
     *
     * @return true if successful and otherwise false
     */
    private static boolean runExportSetup() {
        boolean isExportSetup = false;
        if (os.startsWith("Mac")) {
            EditorAlerts.showSimpleAlert("Use Print Option", "On Macintosh there is no independent PDF export option.  To create a PDF, select 'Print' and from the dropdown at the bottom of the print dialog, 'Save to PDF'.\n\n" +
                    "Export works by a \"PDF printer\".  Recent versions of the MAC OS exclude such printers from the printer list -- preferring to require the internal MAC option.");
        }
        else {
            PrinterJob job = getPrinterJob();
            if (job != null) {
                String current = "None";
                if (pdfPrinter != null) current = pdfPrinter.toString();


                boolean proceed = true;


                String message = "Please select a PDF printer from the following window.\n\n" +
                        "There are a variety of such printers, each with slightly different characteristics.  On PC, 'Microsoft Print to PDF' works fine.\nYou can change this selection from Print/Export Setup.\n\n" +
                        "Current: " + current;


                Alert confirm = EditorAlerts.confirmationAlert("Select Printer", message);
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.get() != OK) proceed = false;


                if (proceed) {
     //               proceed = job.showPrintDialog(null);
                    proceed = job.showPrintDialog(mainStage);


                    if (proceed) {
                        pdfPrinter = job.getPrinter();
                        isExportSetup = true;
                    }
                }
                job.endJob();
            }
        }
        return isExportSetup;
    }

    private static void translateNode(Node node) {
        if (pageLayout.getPageOrientation() == PageOrientation.LANDSCAPE) node.getTransforms().add(new Translate(0, minVertOffset));
        else node.getTransforms().add(new Translate(minHorizOffset, minVertOffset));
    }


    /*
     * Send nodes from print buffer to printer.  Handles header, footer, and page breaks.
     *
     * @param footerInfo the footer string
     * @param job the printer job
     */
    private static void printNodes(String footerInfo, PrinterJob job) {

        MainWindowView.activateProgressIndicator("printing");



    //  System.out.println("printer:" + printer + " layout: " + pageLayout);    //?? problem here


        boolean success = true;
        int pageNum = 0;
        VBox pageBox = new VBox();
        double netHeight = 0;
        int i = 0;

        if (topBox != null) {
            topBox.setMaxWidth(getPageWidth() / baseScale);
            topBox.setMinWidth(getPageWidth() / baseScale);
            PrintBufferItem topBoxItem = getNodeSize(topBox);
            topBoxItem.setScale(baseScale);
            printBuffer.add(0, topBoxItem);
        }

        while (i < printBuffer.size()) {
            PrintBufferItem bufferItem = printBuffer.get(i);
            double scale = bufferItem.getScale();
            double nodeHeight = bufferItem.getHeight() * scale;

            Node node = bufferItem.getNode();
            node.getTransforms().clear();
            node.getTransforms().add(new Scale(scale, scale));
            Group nodeGroup = new Group(bufferItem.getNode());

            double newHeight = nodeHeight + netHeight;

            //if the node fits on the page add to page
            if (newHeight <= pageLayout.getPrintableHeight()) {
                pageBox.getChildren().add(nodeGroup);
                netHeight = newHeight;
                i++;


                //if all the nodes have been added print page
                if (i == printBuffer.size()) {

                    spacer.setPrefHeight(internalPrintHeight - (netHeight + 16.0)) ;  //24  temp fix to stop cutoff footer for vtree
                    pageBox.getChildren().addAll(spacer, getFooterBox(++pageNum, footerInfo));

                    translateNode(pageBox);
                    success = (job.printPage(internalPageLayout, pageBox) && success);
                }
                //if the node does not fit on this page, print page and start new
            } else if (!pageBox.getChildren().isEmpty()) {

                spacer.setPrefHeight(internalPrintHeight - (netHeight + 16.0));  //16
                pageBox.getChildren().addAll(spacer, getFooterBox(++pageNum, footerInfo));

                translateNode(pageBox);
                success = (job.printPage(internalPageLayout, pageBox) && success);


                netHeight = 0;
                pageBox = new VBox();
                //node is too big for a page, truncate and print anyway (human check says ok)
            } else {
                //the pageBox is too tall with this node, so we need another approach
                Rectangle nodeBox = new Rectangle(pageLayout.getPrintableWidth(), pageLayout.getPrintableHeight());
                nodeGroup.setClip(nodeBox);

                HBox footerBox = getFooterBox(++pageNum, footerInfo);
                footerBox.getTransforms().add(new Scale(scale, scale));
                StackPane pane = new StackPane(nodeGroup, footerBox);
                pane.setAlignment(footerBox, Pos.TOP_LEFT);
                footerBox.setTranslateY(internalPrintHeight - 16.0);  //16
                footerBox.setTranslateX(0.0);

                translateNode(pane);
                success = (job.printPage(internalPageLayout, pane) && success);
                i++;
            }
        }
        MainWindowView.deactivateProgressIndicator();
        if (success) EditorAlerts.fleetingPopup("Print job complete.");
        else EditorAlerts.fleetingRedPopup("Print job did not complete.");
    }


    /*
     * Construct footer from page num and info string
     *
     * @param pageNum the page number
     * @param infoString the info string
     *
     * @return the footer HBox
     */
    private static HBox getFooterBox(int pageNum, String infoString) {
        Region spacer = new Region();
        HBox footerBox = new HBox(new Label(Integer.toString(pageNum)), spacer, new Label(infoString));
        footerBox.getTransforms().add(new Scale(baseScale, baseScale));
        footerBox.setHgrow(spacer, Priority.ALWAYS);
        footerBox.setMaxWidth(getPageWidth() / baseScale);
        footerBox.setMinWidth(getPageWidth() / baseScale);

        return footerBox;
    }


    /**
     * Show print dialog and send items from print buffer to the selected printer
     *
     * @param footerInfo the footer info string
     */
    public static void sendBufferToPrint(String footerInfo) {

        PrinterJob job = PrinterJob.createPrinterJob(printer);
        job.setPrinter(printer);
        job.getJobSettings().setPageLayout(pageLayout);

        if (job != null) {

      //      job.showPageSetupDialog(mainStage);
      //      job.setPrinter(printer);

            boolean proceed = job.showPrintDialog(mainStage);
            if (proceed) {
                printNodes(footerInfo, job);
            } else MainWindowView.deactivateProgressIndicator();
            job.endJob();
        }
    }


    /*
     * If pdf printer not already selected, select; then send print buffer to the selected pdf printer
     * @param footerInfo
     */
    public static void sendBufferToPDF(String footerInfo) {
        boolean success = false;
        if (pdfPrinter != null || runExportSetup()) {
            PrinterJob job = PrinterJob.createPrinterJob(pdfPrinter);

            if (job != null) {

                PageLayout minLayout = pdfPrinter.createPageLayout(pageLayout.getPaper(), pageLayout.getPageOrientation(), Printer.MarginType.HARDWARE_MINIMUM);
                minHorizOffset = minLayout.getLeftMargin();
                minVertOffset = minLayout.getTopMargin();

                //Landscape margins do not work as expected and seem different on Win and Mac.  This kludge seems ok?
                double top = pageLayout.getTopMargin();
                double bottom = 18.0;
                double left = pageLayout.getLeftMargin();
                double right = pageLayout.getRightMargin();
                double leftMargin = (left > minHorizOffset) ? left - minHorizOffset : minHorizOffset;
                double topMargin = (top > minVertOffset) ? top - minVertOffset : minVertOffset;

                if (pageLayout.getPageOrientation() == PageOrientation.LANDSCAPE) {
                    internalPageLayout = printer.createPageLayout(pageLayout.getPaper(), pageLayout.getPageOrientation(), leftMargin, right, bottom,topMargin);
                } else {
                    internalPageLayout = printer.createPageLayout(pageLayout.getPaper(), pageLayout.getPageOrientation(), leftMargin, right, topMargin, bottom);
                }

                printNodes(footerInfo, job);
                success = true;
                job.endJob();
            }
        }
        if (!success) EditorAlerts.showSimpleAlert("Print Problem", "Failed to create export job");
    }


    /*
     * Get size of node, and return print buffer item with node height and width
     *
     * @param node the node
     * @return the print buffer item
     */
    private static PrintBufferItem getNodeSize(Node node) {
        Group root = new Group();
        Scene scene = new Scene(root);

        root.getChildren().add(node);
        root.applyCss();
        root.layout();

        Bounds bounds = node.getLayoutBounds();

     //   System.out.println("node: " + node + " width: " + bounds.getWidth());

        return new PrintBufferItem(node, bounds.getHeight(), bounds.getWidth());
    }


    /**
     * Get print buffer item from node, updated with scale values; add to printBuffer
     *
     * @param node the node
     *
     * @return true if the node fits on the page at the base scale, and otherwise false
     */
    public static boolean processPrintNode(Node node) {
        boolean nodeFit = true;
        double wScale = 1.0;
        double hScale = 1.0;
        PrintBufferItem bufferItem = getNodeSize(node);
        double width = bufferItem.getWidth();
        double height = bufferItem.getHeight();



        if (width > getPageWidth() / baseScale) {
            nodeFit = false;
            wScale = getPageWidth() / width;
        }
        if (height > getPageHeight() / baseScale) {
            nodeFit = false;
            hScale = getPageHeight() / height;
        }
        double scale = Math.min(Math.min(wScale, hScale), baseScale);
        bufferItem.setScale(scale);
        printBuffer.add(bufferItem);


        return nodeFit;
    }




    /**
     * Get the selected (printable) page width
     *
     * @return the width
     */
    public static double getPageWidth() {
        return pageWidth.get();
    }


    /**
     * The selected (printable) page with property
     * @return the page width property
     */
    public static DoubleProperty pageWidthProperty() {
        return pageWidth;
    }


    /**
     * Get the selected (printable) page height.  This value does not include footer space.
     *
     * @return the height
     */
    public static double getPageHeight() {
        return pageHeight.get();
    }


    /**
     * The selected (printable) page height property.  This value does not include the footer space.
     *
     * @return the page height property
     */
    public static DoubleProperty pageHeightProperty() {
        return pageHeight;
    }


    /**
     * The page layout includes margins, paper and such
     *
     * @return the currently selected page layout
     */
    public static PageLayout getPageLayout() {
        return pageLayout;
    }


    /**
     * Set page layout and update dependent values
     *
     * @param pageLayout the page layout
     */
    public static void setPageLayout(PageLayout pageLayout) {
        if (pageLayout != null) {
            PrintUtilities.pageLayout = pageLayout;
            pageHeight.set(pageLayout.getPrintableHeight());
            pageWidth.set(pageLayout.getPrintableWidth());

            PageLayout minLayout = printer.createPageLayout(pageLayout.getPaper(), pageLayout.getPageOrientation(), Printer.MarginType.HARDWARE_MINIMUM);
            minHorizOffset = minLayout.getLeftMargin();
            minVertOffset = minLayout.getTopMargin();

            double bottom = 18.0;
            double leftMargin = pageLayout.getLeftMargin();
            double rightMargin = pageLayout.getRightMargin();
            double topMargin = pageLayout.getTopMargin();

            if (leftMargin > minHorizOffset) {
                leftMargin = leftMargin - minHorizOffset;
            }
            if (topMargin > minVertOffset) {
                topMargin = topMargin - minVertOffset;
            }
            if (pageLayout.getPageOrientation() == PageOrientation.LANDSCAPE) {
                internalPrintHeight = pageLayout.getPaper().getWidth() - pageLayout.getTopMargin() - 18.0;
                internalPageLayout = printer.createPageLayout(pageLayout.getPaper(), pageLayout.getPageOrientation(), leftMargin, rightMargin, bottom, topMargin);
            }
            else {
                internalPrintHeight = pageLayout.getPaper().getHeight() - pageLayout.getTopMargin() - 18.0;
                internalPageLayout = printer.createPageLayout(pageLayout.getPaper(), pageLayout.getPageOrientation(), leftMargin, rightMargin, topMargin, bottom);
            }
        }
    }


    /**
     * The currently selected printer
     *
     * @return the printer
     */
    public static Printer getPrinter() {
        return printer;
    }

    public static Printer getPdfPrinter() {
        return pdfPrinter;
    }

    public static void setPdfPrinter(String name) {
        ObservableSet<Printer> printers = Printer.getAllPrinters();
        for (Printer printer : printers) {
            if (printer.getName().equals(name)) {
                pdfPrinter = printer;
            }
        }
    }

    /**
     * Reset scale for members of the print buffer all to the base value
     */
    public static void resetPrintBufferScale() {
        for (PrintBufferItem item : printBuffer) {
            item.setScale(baseScale);
        }
    }


    /**
     * Clear the print buffer and reset the base scale
     *
     * @param baseScale the base scale value
     */
    public static void resetPrintBuffer(double baseScale) {
        PrintUtilities.baseScale = baseScale;
        printBuffer.clear();
        topBox = null;
    }


    /**
     * Set the header (top) box
     *
     * @param box the top box
     */
    public static void setTopBox(VBox box) { topBox = box; }
}
