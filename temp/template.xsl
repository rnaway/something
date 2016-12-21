<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
<xsl:template match="logMessages">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
		<fo:layout-master-set>
		<fo:simple-page-master page-height="297mm" page-width="210mm" margin-top="10mm" margin-left="20mm" margin-right="20mm" margin-bottom="10mm" master-name="PageMaster">
				<fo:region-before background-color="#EFEFEF" extent="40pt" />
				<fo:region-after background-color="#EFEFEF" extent="40pt" />
				<fo:region-start background-color="#EFAFEF" extent="40pt" reference-orientation="270" />
				<fo:region-end background-color="#EFAFEF" extent="40pt" reference-orientation="270" />
				<fo:region-body background-color="#EFFFAF" margin-top="56pt" margin-left="50pt" margin-right="50pt" margin-bottom="56pt" />
			</fo:simple-page-master>
		</fo:layout-master-set>
      <fo:page-sequence master-reference="simpleA4">
        <fo:flow flow-name="xsl-region-body">
          <fo:block font-size="10pt">   
              <xsl:apply-templates select="logMessage"/>
          </fo:block>
        </fo:flow>
      </fo:page-sequence>
     </fo:root>
</xsl:template>
<xsl:template match="logMessage">

	
		<fo:page-sequence reference-orientation="from-page-master-region()" master-reference="PageMaster">
		<fo:static-content flow-name="xsl-region-start">
			<fo:block-container height="100%" display-align="center">
				<fo:block text-align="center" font-family="sans-serif" font-size="1.6em">Date</fo:block>
			</fo:block-container>
		</fo:static-content>
		
		<fo:static-content flow-name="xsl-region-start">
			<fo:block-container height="100%" display-align="center">
				<fo:block text-align="center" font-family="sans-serif" font-size="1.6em">Message</fo:block>
			</fo:block-container>
		</fo:static-content>
		
		<fo:flow flow-name="xsl-region-body" font-family="serif" font-size="1em" line-height="1.4" axf:hyphenate-hyphenated-word="false" hyphenate="true">
		<fo:block span="all" font-family="sans-serif" font-size="2em" font-weight="bold" line-height="2" space-before="1em" space-after="1em" space-before.conditionality="retain" space-after.conditionality="retain" background-color="transparent">
		<fo:block start-indent="1em" end-indent="1em">Explaining each region in a page</fo:block>
		</fo:block>
		<fo:block start-indent="0.5em" end-indent="0.5em" hyphenate="true" text-align="justify">
		<fo:block>One page consists of five regions in XSL-FO.</fo:block>
		<fo:block space-before="1em"><fo:inline font-weight="bold" space-end="1em">1. region-start</fo:inline>Region of the left side where 'left' is written. The length from the left edge of the paper to the left edge of the region-start can be specified by the margin-left property. The width of region-start can be specified by the extent property.</fo:block>
	<fo:block space-before="1em"><fo:inline font-weight="bold" space-end="1em">2. region-end</fo:inline>Region of the right side where 'right' is written. The length from the right edge of the paper to the right edge of the region-end can be specified by the margin-end property. The width of region-end can be specified by the extent property.</fo:block>
	<fo:block space-before="1em"><fo:inline font-weight="bold" space-end="1em">3. region-before</fo:inline>The upper region where 'top' is placed. The length from the top edge of the paper to the top edge of the region-before can be specified by the margin-top property. The depth ofregion-before can be specified by the extent property. This will be the most commonly used to create headers.</fo:block>
	<fo:block space-before="1em"><fo:inline font-weight="bold" space-end="1em">4. region-after</fo:inline>The lower region where 'Antenna House, Inc.' is placed. The length from the bottom edge of the paper to the bottom edge of the region-after can be specified by the margin-bottom property. The depth of region-after can be specified by the extent property. This will be the most commonly used to create footers.</fo:block>
	<fo:block space-before="1em"><fo:inline font-weight="bold" space-end="1em">5. region-body</fo:inline>The middle region of the page where the text is placed.</fo:block>
		</fo:block>
	</fo:flow>
		
		
		
  </xsl:template>
</xsl:stylesheet>