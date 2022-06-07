<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	
	<xsl:param name="with_names" select="/data/ReportOptions/ReportOption[Name='with_names']/Value"/>
	<xsl:param name="only_unfinished" select="number(concat('0', /data/ReportOptions/ReportOption[Name='only_unfinished']/Value))"/>
 	
	<xsl:include href="./includes/standard.xsl" />
	<xsl:include href="./includes/racecompheader.xsl" />	
	
	<xsl:template name="body">
		<!-- comps -->
		<xsl:if test="$only_unfinished = 0">
			<xsl:apply-templates select="/data/Comps/Comp" />
		</xsl:if>
		
		<xsl:if test="$only_unfinished = 1">
			<!-- sign, XPath 1.0 sucks -->
			<xsl:apply-templates select="/data/Comps/Comp[Dummy = 0 and (State = 1 or Cancelled = 1)][1] | /data/Comps/Comp[Dummy = 0 and (State = 1 or Cancelled = 1)][1]/following-sibling::Comp[State = 1]"/>
		</xsl:if>

		<!-- cancelled boats -->
		<xsl:if test="count(/data/RaceInfo) = 1 and count(/data/Entries/Entrie[Entry_CancelValue != 0]) &gt; 0">
			<fo:block font-size="10pt">
				<fo:block font-weight="bold">abgemeldete Boote</fo:block>
				<fo:list-block padding-top="0.25em" padding-bottom="0.25em" border="medium solid black" provisional-label-separation="10pt" provisional-distance-between-starts="3.5em">
					<xsl:for-each select="/data/Entries/Entrie[Entry_CancelValue != 0]">
						<xsl:sort select="Entry_Bib" data-type="number" />
						<fo:list-item>
							<!-- Bib -->
							<fo:list-item-label end-indent="label-end()">
								<fo:block text-align="right">
									<xsl:value-of select="Entry_Bib" />
								</fo:block>
							</fo:list-item-label>

							<!-- Entry Label and Crew -->
							<fo:list-item-body start-indent="body-start()">
								<fo:block>
									<xsl:call-template name="GetEntryLabel">
										<xsl:with-param name="entry" select="." />
									</xsl:call-template>
								</fo:block>
							</fo:list-item-body>
						</fo:list-item>
					</xsl:for-each>
				</fo:list-block>
			</fo:block>
		</xsl:if>
	</xsl:template>
	
	
	<xsl:template match="Comp[Dummy != 1]">
		<xsl:variable name="cid" select="ID" />
		<xsl:variable name="rid" select="Race_ID_FK" />
		<xsl:variable name="round" select="Round" />
		<xsl:variable name="boats" select="/data/Lanes/Lane[CE_Comp_ID_FK = $cid]" />
		
		<fo:table width="100%" table-layout="fixed">
			<fo:table-column column-number="1" column-width="1cm" />
			<fo:table-column column-number="2" column-width="16cm" />

			<xsl:if test="string-length(normalize-space(RuleText)) &gt; 0 and $show_qrule and Cancelled = 0">
				<fo:table-footer>
					<fo:table-row>
						<fo:table-cell number-columns-spanned="3">
							<fo:block margin="0.25em 0" padding="0.25em" font-size="8pt">
								<xsl:text>Qualifikation: </xsl:text>
								<xsl:value-of select="./RuleText"/>
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
				</fo:table-footer>
			</xsl:if>

			<!-- lanes -->
			<fo:table-body font-size="10pt" keep-with-previous="always">
				<fo:table-row font-size="1pt">
					
					<fo:table-cell><fo:block text-align="center"></fo:block></fo:table-cell>
					<fo:table-cell><fo:block></fo:block></fo:table-cell>
				</fo:table-row>
				
				<xsl:for-each select="$boats">
					<xsl:sort select="CE_Lane" data-type="number"/>
					<xsl:variable name="eid" select="CE_Entry_ID_FK" />
					<xsl:variable name="entry" select="/data/Entries/Entrie[Entry_ID = $eid]" />
					
					<fo:table-row margin-top="0.25em">
						<xsl:attribute name="keep-with-previous">
							<xsl:if test="count($boats) &gt; 8">auto</xsl:if>
							<xsl:if test="count($boats) &lt;= 8">always</xsl:if>
						</xsl:attribute>
						<xsl:if test="$entry/Entry_CancelValue != 0">
							<xsl:attribute name="text-decoration">line-through</xsl:attribute>
						</xsl:if>
						
						<fo:table-cell>
							<fo:block text-align="center">
								<xsl:value-of select="$entry/Entry_Bib" />
							</fo:block>
						</fo:table-cell>
						<fo:table-cell>
							<fo:block font-weight="bold">
								<xsl:call-template name="GetEntryLabel">
									<xsl:with-param name="entry" select="$entry" />
								</xsl:call-template>
								<xsl:if test="$entry/Entry_CancelValue != 0">
									<fo:inline text-decoration="none"> (abgemeldet)</fo:inline>
								</xsl:if>
							</fo:block>

							<!-- names -->
							<xsl:if test="$with_names = 1">
								<fo:block font-size="9pt" keep-with-previous="always" margin-bottom="1em">
									<xsl:apply-templates select="$entry" mode="GetCrewString">
										<xsl:with-param name="round" select="$round"/>
									</xsl:apply-templates>
								</fo:block>
							</xsl:if>
						</fo:table-cell>
					</fo:table-row>
				</xsl:for-each>
			</fo:table-body>
		</fo:table>
	</xsl:template>
	
	<xsl:template match="Comp[Dummy = 1]">
		<fo:block border="medium solid black" margin-bottom="1em" font-weight="bold" font-size="12pt">
			<fo:block margin="2pt">
				<xsl:value-of select="Label"/>
			</fo:block>
		</fo:block>
	</xsl:template>
	
</xsl:stylesheet>
