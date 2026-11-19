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
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
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


import slapp.editor.main_window.MainWindow;
import slapp.editor.main_window.MainWindowView;

import javafx.print.PageRange;

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

    // Reusable off-screen Scene for CSS & layout measurement on the FX thread
    private static final Group measureRoot = new Group();
    private static final Scene measureScene = new Scene(measureRoot);

    private static boolean printInProgress = false;

    /**
     * Set default print values
     */
    static {
        spacer.setVisible(false);
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            PageLayout baseLayout = job.getJobSettings().getPageLayout();
            pageHeight.set(baseLayout.getPrintableHeight());

            //       System.out.println("Print pg height: " + pageHeight);

            pageWidth.set(baseLayout.getPrintableWidth());
            printer = job.getPrinter();
            double bottomMargin = Math.max(baseLayout.getBottomMargin(), 48);
            pageLayout = printer.createPageLayout(baseLayout.getPaper(), baseLayout.getPageOrientation(), baseLayout.getLeftMargin(), baseLayout.getRightMargin(), baseLayout.getTopMargin(), bottomMargin);

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
            } else {
                internalPrintHeight = pageLayout.getPaper().getHeight() - pageLayout.getTopMargin() - 18.0;
                internalPageLayout = printer.createPageLayout(pageLayout.getPaper(), pageLayout.getPageOrientation(), leftMargin, rightMargin, topMargin, bottom);
            }

        } else {
            pageHeight.set(684);
            pageWidth.set(504);
        }
    }


    private static PrinterJob getPrinterJob() {
        // --- FIX 1: Force job to use selected printer instead of blindly returning to OS default ---
        PrinterJob job = (printer != null) ? PrinterJob.createPrinterJob(printer) : PrinterJob.createPrinterJob();

        if (job == null) {
            EditorAlerts.showSimpleAlert("Cannot find Printer", "I do not find an installed printer.  Please install printer and try again.");
        } else {
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
        pageLayout = printer.createPageLayout(baseLayout.getPaper(), baseLayout.getPageOrientation(), baseLayout.getLeftMargin(), baseLayout.getRightMargin(), baseLayout.getTopMargin(), bottomM);

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
        } else {
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

            MainWindow.restoreMacFocus(mainStage);

            if (proceed) {
                PageLayout baseLayout = job.getJobSettings().getPageLayout();
                double bottomMargin = Math.max(baseLayout.getBottomMargin(), 48);
                pageLayout = printer.createPageLayout(baseLayout.getPaper(), baseLayout.getPageOrientation(), baseLayout.getLeftMargin(), baseLayout.getRightMargin(), baseLayout.getTopMargin(), bottomMargin);
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
                } else {
                    internalPrintHeight = pageLayout.getPaper().getHeight() - pageLayout.getTopMargin() - 18.0;
                    internalPageLayout = printer.createPageLayout(pageLayout.getPaper(), pageLayout.getPageOrientation(), leftMargin, rightMargin, topMargin, bottom);
                }

            }
            job.endJob();
        }
    }

    private static boolean refreshPageLayout() {
        PrinterJob job = getPrinterJob();
        if (job != null) {
            job.getJobSettings().setPageLayout(pageLayout);

            PageLayout baseLayout = job.getJobSettings().getPageLayout();
            double bottomMargin = Math.max(baseLayout.getBottomMargin(), 48);
            pageLayout = printer.createPageLayout(baseLayout.getPaper(), baseLayout.getPageOrientation(), baseLayout.getLeftMargin(), baseLayout.getRightMargin(), baseLayout.getTopMargin(), bottomMargin);
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
            } else {
                internalPrintHeight = pageLayout.getPaper().getHeight() - pageLayout.getTopMargin() - 18.0;
                internalPageLayout = printer.createPageLayout(pageLayout.getPaper(), pageLayout.getPageOrientation(), leftMargin, rightMargin, topMargin, bottom);
            }
            job.endJob();
            return true;
        }
        return false;
    }

    /**
     * Select export printer with failure message
     */
    public static void exportSetup() {
        if (!runExportSetup()) EditorAlerts.showSimpleAlert("Print Problem", "Export setup failed");
    }


    /**
     * Select export printer with print dialog
     *
     * @return true if successful and otherwise false
     */
    private static boolean runExportSetup() {
        boolean isExportSetup = false;
        if (os.startsWith("mac")) {
            EditorAlerts.showSimpleAlert("Use Print Option", "On Macintosh there is no independent PDF export option.  To create a PDF, select 'Print' and from the dropdown at the bottom of the print dialog, 'Save to PDF'.\n\n" +
                    "Export works by a \"PDF printer\".  Recent versions of the MAC OS exclude such printers from the printer list -- preferring to require the internal MAC option.");
        } else {
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
        if (pageLayout.getPageOrientation() == PageOrientation.LANDSCAPE)
            node.getTransforms().add(new Translate(0, minVertOffset));
        else node.getTransforms().add(new Translate(minHorizOffset, minVertOffset));
    }

    /*
     * Assemble printable page containers on the FX Application Thread.
     */
    private static List<Parent> buildPrintPages(String footerInfo) {
        List<Parent> pages = new ArrayList<>();
        int pageNum = 0;
        VBox pageBox = new VBox();
        double netHeight = 0;
        int i = 0;

        // Reserve enough vertical space for the scaled footerBox plus bottom clip clearance
        double footerOffset = Math.max(26.0, Math.ceil(20.0 * baseScale));

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
            Group nodeGroup = new Group(node);

            double newHeight = nodeHeight + netHeight;

            // If the node fits on the page, add to page
            if (newHeight <= pageLayout.getPrintableHeight()) {
                pageBox.getChildren().add(nodeGroup);
                netHeight = newHeight;
                i++;

                // If all the nodes have been added, finalize page
                if (i == printBuffer.size()) {
                    Region pageSpacer = new Region();
                    pageSpacer.setVisible(false);
                    pageSpacer.setPrefHeight(Math.max(0.0, internalPrintHeight - (netHeight + footerOffset)));
                    pageBox.getChildren().addAll(pageSpacer, getFooterBox(++pageNum, footerInfo));

                    translateNode(pageBox);
                    pages.add(pageBox);
                }
                // If the node does not fit on this page, finalize current page and start a new one
            } else if (!pageBox.getChildren().isEmpty()) {
                Region pageSpacer = new Region();
                pageSpacer.setVisible(false);
                pageSpacer.setPrefHeight(Math.max(0.0, internalPrintHeight - (netHeight + footerOffset)));
                pageBox.getChildren().addAll(pageSpacer, getFooterBox(++pageNum, footerInfo));

                translateNode(pageBox);
                pages.add(pageBox);

                netHeight = 0;
                pageBox = new VBox();
                // Node is too big for a single page; clip and print anyway
            } else {
                Rectangle nodeBox = new Rectangle(pageLayout.getPrintableWidth(), pageLayout.getPrintableHeight());
                nodeGroup.setClip(nodeBox);

                HBox footerBox = getFooterBox(++pageNum, footerInfo);
                StackPane pane = new StackPane(nodeGroup, footerBox);
                StackPane.setAlignment(nodeGroup, Pos.TOP_LEFT);
                StackPane.setAlignment(footerBox, Pos.TOP_LEFT);
                footerBox.setTranslateY(internalPrintHeight - footerOffset);
                footerBox.setTranslateX(0.0);

                translateNode(pane);
                pages.add(pane);
                i++;
            }
        }
        return pages;
    }

    private static boolean isPageInRange(int pageNumber, PageRange[] pageRanges) {
        if (pageRanges == null || pageRanges.length == 0) {
            return true;
        }
        for (PageRange range : pageRanges) {
            if (pageNumber >= range.getStartPage() && pageNumber <= range.getEndPage()) {
                return true;
            }
        }
        return false;
    }

    /*
     * Build page nodes on the FX thread, then spool to PrinterJob on a background thread
     * so the ProgressIndicator continues spinning smoothly.
     */
    private static void printNodes(String footerInfo, PrinterJob job, boolean restoreMacFocus) {
        MainWindowView.activateProgressIndicator("printing");

        // 1. Build all page Scene-graph containers on the FX Application Thread
        //    so pagination and footer page numbers are accurate across the document.
        List<Parent> pages = buildPrintPages(footerInfo);

        // 2. Capture any user-selected page ranges from the print dialog, then clear
        //    the attribute on JobSettings (JDK-8150076) so we filter the pages ourselves
        //    and Java2D does not prematurely close the job or misalign start pages.
        PageRange[] selectedRanges = job.getJobSettings().getPageRanges();
        if (selectedRanges != null && selectedRanges.length > 0) {
            job.getJobSettings().setPageRanges((PageRange[]) null);
        }

        // 3. Spool only the requested pages and endJob() on a background thread
        Task<Boolean> printTask = new Task<>() {
            @Override
            protected Boolean call() {
                boolean success = true;
                boolean printedAnyPage = false;

                for (int i = 0; i < pages.size(); i++) {
                    int pageNumber = i + 1; // 1-based page number matching Print Dialog
                    if (isPageInRange(pageNumber, selectedRanges)) {
                        printedAnyPage = true;
                        if (!job.printPage(internalPageLayout, pages.get(i))) {
                            success = false;
                            break;
                        }
                    }
                }

                if (!printedAnyPage) {
                    job.cancelJob();
                    return false;
                }

                if (success) {
                    success = job.endJob();
                } else {
                    job.endJob();
                }
                return success;
            }
        };

        printTask.setOnSucceeded(e -> {
            setPrintInProgress(false);
            MainWindowView.deactivateProgressIndicator();
            if (restoreMacFocus) {
                MainWindow.restoreMacFocus(mainStage);
            }
            if (Boolean.TRUE.equals(printTask.getValue())) {
                EditorAlerts.fleetingPopup("Print job complete.");
            } else {
                EditorAlerts.fleetingRedPopup("Print job did not complete.");
            }
        });

        printTask.setOnFailed(e -> {
            job.endJob();
            setPrintInProgress(false);
            MainWindowView.deactivateProgressIndicator();
            if (restoreMacFocus) {
                MainWindow.restoreMacFocus(mainStage);
            }
            EditorAlerts.fleetingRedPopup("Print job did not complete.");
        });

        Thread printThread = new Thread(printTask, "SLAPP-PrintSpooler");
        printThread.setDaemon(true);
        printThread.start();
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
        PrinterJob job = (printer != null) ? PrinterJob.createPrinterJob(printer) : PrinterJob.createPrinterJob();
        if (job != null) {
            if (printer != null) {
                job.setPrinter(printer);
            }
            job.getJobSettings().setPageLayout(pageLayout);
        }

        Platform.runLater(() -> {
            if (job != null) {
                if (refreshPageLayout()) {
                    // Pause spinner while native modal print dialog waits for user input
                    MainWindowView.deactivateProgressIndicator();

                    if (job.showPrintDialog(mainStage)) {
                        Printer selectedPrinter = job.getPrinter();
                        PageLayout selectedLayout = job.getJobSettings().getPageLayout();

                        double bottomMargin = Math.max(selectedLayout.getBottomMargin(), 48);
                        pageLayout = selectedPrinter.createPageLayout(selectedLayout.getPaper(),
                                selectedLayout.getPageOrientation(), selectedLayout.getLeftMargin(),
                                selectedLayout.getRightMargin(), selectedLayout.getTopMargin(), bottomMargin);

                        pageHeight.set(pageLayout.getPrintableHeight());
                        pageWidth.set(pageLayout.getPrintableWidth());

                        PageLayout minLayout = selectedPrinter.createPageLayout(selectedLayout.getPaper(),
                                pageLayout.getPageOrientation(), Printer.MarginType.HARDWARE_MINIMUM);
                        minHorizOffset = minLayout.getLeftMargin();
                        minVertOffset = minLayout.getTopMargin();

                        double bottom = Math.max(18.0, minLayout.getBottomMargin());
                        double leftMargin = Math.max(0.0, pageLayout.getLeftMargin() - minHorizOffset);
                        double rightMargin = pageLayout.getRightMargin();
                        double topMargin = Math.max(0.0, pageLayout.getTopMargin() - minVertOffset);

                        if (pageLayout.getPageOrientation() == PageOrientation.LANDSCAPE) {
                            internalPageLayout = selectedPrinter.createPageLayout(pageLayout.getPaper(),
                                    pageLayout.getPageOrientation(), leftMargin, rightMargin, bottom, topMargin);
                        } else {
                            internalPageLayout = selectedPrinter.createPageLayout(pageLayout.getPaper(),
                                    pageLayout.getPageOrientation(), leftMargin, rightMargin, topMargin, bottom);
                        }

                        // Exact unclipped vertical space remaining after translateNode shifts down by minVertOffset
                        internalPrintHeight = internalPageLayout.getPrintableHeight() - minVertOffset;

                        printer = selectedPrinter;

                        // Spools on a background Task and resets printInProgress when done
                        printNodes(footerInfo, job, true);
                        return;
                    }

                    MainWindow.restoreMacFocus(mainStage);
                }
                job.endJob();
                setPrintInProgress(false);
                MainWindowView.deactivateProgressIndicator();
            } else {
                setPrintInProgress(false);
                MainWindowView.deactivateProgressIndicator();
            }
        });
    }

    /*
     * If pdf printer not already selected, select; then send print buffer to the selected pdf printer
     * @param footerInfo
     */
    public static void sendBufferToPDF(String footerInfo) {
        if (pdfPrinter == null) {
            // Pause spinner while user selects PDF printer in modal dialog
            MainWindowView.deactivateProgressIndicator();
            if (!runExportSetup()) {
                setPrintInProgress(false);
                EditorAlerts.showSimpleAlert("Print Problem", "Failed to create export job");
                return;
            }
        }

        PrinterJob job = PrinterJob.createPrinterJob(pdfPrinter);
        if (job != null) {
            PageLayout minLayout = pdfPrinter.createPageLayout(pageLayout.getPaper(),
                    pageLayout.getPageOrientation(), Printer.MarginType.HARDWARE_MINIMUM);
            minHorizOffset = minLayout.getLeftMargin();
            minVertOffset = minLayout.getTopMargin();

            double bottom = Math.max(18.0, minLayout.getBottomMargin());
            double leftMargin = Math.max(0.0, pageLayout.getLeftMargin() - minHorizOffset);
            double rightMargin = pageLayout.getRightMargin();
            double topMargin = Math.max(0.0, pageLayout.getTopMargin() - minVertOffset);

            if (pageLayout.getPageOrientation() == PageOrientation.LANDSCAPE) {
                internalPageLayout = pdfPrinter.createPageLayout(pageLayout.getPaper(),
                        pageLayout.getPageOrientation(), leftMargin, rightMargin, bottom, topMargin);
            } else {
                internalPageLayout = pdfPrinter.createPageLayout(pageLayout.getPaper(),
                        pageLayout.getPageOrientation(), leftMargin, rightMargin, topMargin, bottom);
            }

            // Exact unclipped vertical space remaining after translateNode shifts down by minVertOffset
            internalPrintHeight = internalPageLayout.getPrintableHeight() - minVertOffset;

            // Spools on a background Task and resets printInProgress when done
            printNodes(footerInfo, job, false);
        } else {
            setPrintInProgress(false);
            MainWindowView.deactivateProgressIndicator();
            EditorAlerts.showSimpleAlert("Print Problem", "Failed to create export job");
        }
    }


    /*
     * Get size of node, and return print buffer item with node height and width
     *
     * @param node the node
     * @return the print buffer item
     */
    private static PrintBufferItem getNodeSize(Node node) {
        measureRoot.getChildren().setAll(node);
        measureRoot.applyCss();
        measureRoot.layout();

        Bounds bounds = node.getLayoutBounds();
        double height = bounds.getHeight();
        double width = bounds.getWidth();

        // Detach immediately so node has no parent/Scene reference before printing
        measureRoot.getChildren().clear();

        return new PrintBufferItem(node, height, width);
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

        //      System.out.println(node + " : " + width + "/" + height);



        if (width > getPageWidth() / baseScale) {
            nodeFit = false;
            wScale = getPageWidth() / width;
        }
        if (height > (getPageHeight() + 15) / baseScale) {
            nodeFit = false;
            hScale = (getPageHeight() + 15)/ height;
        }
        double scale = Math.min(Math.min(wScale, hScale), baseScale);
        bufferItem.setScale(scale);
        printBuffer.add(bufferItem);


        return nodeFit;
    }

    public static void clearPrintBuffer() {
        printBuffer.clear();
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

    public static boolean isPrintInProgress() {
        return printInProgress;
    }

    public static void setPrintInProgress(boolean inProgress) {
        printInProgress = inProgress;
    }
}