<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:odm="http://www.cdisc.org/ns/odm/v1.3"
	xmlns:OpenClinica="http://www.openclinica.org/ns/odm_ext_v130/v3.1" xmlns:xf="http://www.w3.org/2002/xforms">

	<xsl:output method="xml" version='1.0' encoding='UTF-8' indent="yes" omit-xml-declaration="yes" />
	<xsl:strip-space elements="*" />

	<xsl:key name="kLabelsInForm" match="OpenClinica:SectionLabel" use="concat(../@FormOID, '+', .)" />

	<xsl:template match="/">
		<study>
			<xsl:attribute name="name"><xsl:value-of select="//odm:StudyName" /></xsl:attribute>
			<xsl:attribute name="studyKey"><xsl:value-of select="//odm:Study/@OID" /></xsl:attribute>
			<xsl:attribute name="description"><xsl:value-of select="normalize-space(//odm:StudyDescription)" /></xsl:attribute>


			<xsl:for-each select="//*[local-name()='FormDef']">

				<xsl:variable name="vFormOID" select="@OID" />

				<form>
					<xsl:attribute name="name">
						<xsl:value-of select="@Name" />
					</xsl:attribute>
					<xsl:attribute name="description">
						<xsl:value-of select="@OID" />
					</xsl:attribute>
					<version>
						<xsl:attribute name="name"><xsl:value-of select="@Name" />-v1</xsl:attribute>
						<xsl:attribute name="description">Converted from ODM using the oc-conversion-tools</xsl:attribute>
						<xform>
							<xsl:apply-templates select="current()" />
						</xform>
					</version>
				</form>
			</xsl:for-each>
		</study>
	</xsl:template>

	<!-- Create data nodes for the question answers -->
	<xsl:template match="//*[local-name()='FormDef']">

		<xsl:variable name="vForm" select="current()" />

		<xforms xmlns="http://www.w3.org/2002/xforms" xmlns:xsd="http://www.w3.org/2001/XMLSchema">

			<xsl:apply-templates select="$vForm" mode="createModel">
				<xsl:with-param name="pForm" select="$vForm" />
			</xsl:apply-templates>

			<xsl:call-template name="createGroups">
				<xsl:with-param name="pForm" select="$vForm" />
			</xsl:call-template>

		</xforms>
	</xsl:template>

	<!-- Create the Model element -->
	<xsl:template match="//*[local-name()='FormDef']" mode="createModel">

		<xsl:param name="pForm" />

		<xf:model>
			<xsl:variable name="vInstanceElementName">
				<xsl:value-of select="@OID" />
			</xsl:variable>
			<xf:instance>
				<xsl:attribute name="id"><xsl:value-of select="normalize-space($vInstanceElementName)" /></xsl:attribute>
				
				<xsl:element name="{$vInstanceElementName}">
					<xsl:attribute name="Description">This Xform was converted from an ODM file using the oc-conversion-tools</xsl:attribute>
					<xsl:attribute name="name"><xsl:value-of select="@Name" /></xsl:attribute>
					<xsl:attribute name="formKey"><xsl:value-of select="@OID" /></xsl:attribute>
					<xsl:attribute name="StudyOID"><xsl:value-of select="../../@OID" /></xsl:attribute>
					<xsl:attribute name="MetaDataVersionOID"><xsl:value-of select="../@OID" /></xsl:attribute>

					<xsl:element name="subjectkey" />

					<xsl:for-each select="odm:ItemGroupRef">

						<xsl:variable name="vItemGroupOID" select="@ItemGroupOID" />
						<xsl:variable name="vItemGroupDef" select="//*[local-name()='ItemGroupDef' and @OID=$vItemGroupOID and */*/@FormOID=$pForm/@OID]" />

						<xsl:choose>
							<xsl:when test="$vItemGroupDef/@Repeating = 'Yes'">
								<xsl:element name="{@ItemGroupOID}">

									<xsl:for-each select="$vItemGroupDef/odm:ItemRef">

										<xsl:variable name="vItemOID" select="@ItemOID" />
										<xsl:element name="{$vItemOID}" />

									</xsl:for-each>
								</xsl:element>
							</xsl:when>
							<xsl:otherwise>
								<xsl:for-each select="$vItemGroupDef/odm:ItemRef">

									<xsl:variable name="vItemOID" select="@ItemOID" />
									<xsl:variable name="vItemDef" select="//*[local-name()='ItemDef' and @OID=$vItemOID]" />

									<xsl:if test="$vItemDef/*/*/*[local-name()='ItemHeader'] and $vItemDef/*/*/*[local-name()='ItemSubHeader']">
										<xsl:element name="{$vItemOID}_HEADER" />
									</xsl:if>

									<xsl:if test="$vItemDef/*/*/*[local-name()='ItemSubHeader']">
										<xsl:element name="{$vItemOID}_SUB_HEADER" />
									</xsl:if>

									<xsl:element name="{$vItemOID}">
										<xsl:attribute name="ItemGroupOID">
										<xsl:value-of select="$vItemGroupOID" />
									</xsl:attribute>
									</xsl:element>
								</xsl:for-each>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</xsl:element>
				
			</xf:instance>

			<xf:bind>
				<xsl:attribute name="id">subjectkey</xsl:attribute>
				<xsl:attribute name="nodeset">/<xsl:value-of select="$pForm/@OID"/>/subjectkey</xsl:attribute>
				<xsl:attribute name="type">xsd:string</xsl:attribute>
				<xsl:attribute name="locked">true()</xsl:attribute>
				<xsl:attribute name="visible">false()</xsl:attribute>
			</xf:bind>

			<xsl:apply-templates select="child::node()">
				<xsl:with-param name="pForm" select="$pForm" />
			</xsl:apply-templates>
		</xf:model>

	</xsl:template>

	<!-- Create Binds for the questions -->
	<xsl:template match="//*[local-name()='ItemGroupRef']">

		<xsl:param name="pForm" />

		<xsl:variable name="vItemGroupOID" select="@ItemGroupOID" />
		<xsl:variable name="vItemGroupDef" select="//*[local-name()='ItemGroupDef' and @OID=$vItemGroupOID and */*/@FormOID=$pForm/@OID]" />

		<xsl:choose>
			<xsl:when test="$vItemGroupDef/@Repeating = 'Yes'">

				<xf:bind>
					<xsl:attribute name="id"><xsl:value-of select="$vItemGroupOID" /></xsl:attribute>
					<xsl:attribute name="nodeset">/<xsl:value-of select="$pForm/@OID"/>/<xsl:value-of select="$vItemGroupOID" /></xsl:attribute>

					<xsl:if test="./Mandatory = 'Yes'">
						<xsl:attribute name="required">true()</xsl:attribute>
					</xsl:if>
				</xf:bind>

				<xsl:for-each select="$vItemGroupDef/odm:ItemRef">

					<xsl:variable name="vItemOID" select="@ItemOID" />
					<xsl:variable name="vItemDef" select="//*[local-name()='ItemDef' and @OID=$vItemOID]" />

					<xf:bind>
						<xsl:attribute name="id"><xsl:value-of select="@ItemOID" /></xsl:attribute>
						<xsl:attribute name="nodeset">/<xsl:value-of select="$pForm/@OID"/>/<xsl:value-of select="$vItemGroupOID" />/<xsl:value-of select="@ItemOID" /></xsl:attribute>
						<xsl:call-template name="appendQuestionType">
							<xsl:with-param name="pItemDef" select="$vItemDef" />
						</xsl:call-template>
					</xf:bind>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="createItemBinds">
					<xsl:with-param name="pForm" select="$pForm" />
					<xsl:with-param name="pItemGroupDef" select="$vItemGroupDef" />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="createItemBinds">

		<xsl:param name="pForm" />
		<xsl:param name="pItemGroupDef" />

		<xsl:for-each select="$pItemGroupDef/odm:ItemRef">

			<xsl:variable name="vItemOID" select="@ItemOID" />
			<xsl:variable name="vItemDef" select="//*[local-name()='ItemDef' and @OID=$vItemOID]" />

			<xsl:choose>
				<xsl:when test="$vItemDef/*/*/*[local-name()='ItemSubHeader']">

					<xsl:if test="$vItemDef/*/*/*[local-name()='ItemHeader'] and $vItemDef/*/*/*[local-name()='ItemSubHeader']">
						<xf:bind>
							<xsl:attribute name="id"><xsl:value-of select="$vItemOID" />_HEADER</xsl:attribute>
							<xsl:attribute name="nodeset">/<xsl:value-of select="$pForm/@OID"/>/<xsl:value-of select="$vItemOID" />_HEADER</xsl:attribute>
							<xsl:attribute name="locked">true()</xsl:attribute>

							<xsl:call-template name="appendQuestionType">
								<xsl:with-param name="pItemDef" select="$vItemDef" />
							</xsl:call-template>
						</xf:bind>
					</xsl:if>

					<xsl:if test="$vItemDef/*/*/*[local-name()='ItemSubHeader']">

						<xf:bind>
							<xsl:attribute name="id"><xsl:value-of select="$vItemOID" />_SUB_HEADER</xsl:attribute>
							<xsl:attribute name="nodeset">/<xsl:value-of select="$pForm/@OID"/>/<xsl:value-of select="$vItemOID" />_SUB_HEADER</xsl:attribute>
							<xsl:attribute name="locked">true()</xsl:attribute>

							<xsl:call-template name="appendQuestionType">
								<xsl:with-param name="pItemDef" select="$vItemDef" />
							</xsl:call-template>
						</xf:bind>
					</xsl:if>


					<xf:bind>
						<xsl:attribute name="id"><xsl:value-of select="@ItemOID" /></xsl:attribute>
						<xsl:attribute name="nodeset">/<xsl:value-of select="$pForm/@OID"/>/<xsl:value-of select="@ItemOID" /></xsl:attribute>

						<xsl:call-template name="appendQuestionType">
							<xsl:with-param name="pItemDef" select="$vItemDef" />
						</xsl:call-template>

						<xsl:if test="./@Mandatory = 'Yes'">
							<xsl:attribute name="required">true()</xsl:attribute>
						</xsl:if>
					</xf:bind>

				</xsl:when>
				<xsl:otherwise>
					<xf:bind>

						<xsl:attribute name="id"><xsl:value-of select="@ItemOID" /></xsl:attribute>
						<xsl:attribute name="nodeset">/<xsl:value-of select="$pForm/@OID"/>/<xsl:value-of select="@ItemOID" /></xsl:attribute>

						<xsl:call-template name="appendQuestionType">
							<xsl:with-param name="pItemDef" select="$vItemDef" />
						</xsl:call-template>

						<xsl:if test="./@Mandatory = 'Yes'">
							<xsl:attribute name="required">true()</xsl:attribute>
						</xsl:if>

					</xf:bind>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="appendQuestionType">
		<xsl:param name="pItemDef" />
		<xsl:choose>
			<xsl:when test="$pItemDef/@DataType = 'integer'">
				<xsl:attribute name="type">xsd:int</xsl:attribute>
			</xsl:when>
			<xsl:when test="$pItemDef/@DataType = 'float'">
				<xsl:attribute name="type">xsd:decimal</xsl:attribute>
			</xsl:when>
			<xsl:when test="$pItemDef/@DataType = 'date'">
				<xsl:attribute name="type">xsd:date</xsl:attribute>
			</xsl:when>
			<xsl:when test="$pItemDef/@DataType = 'time'">
				<xsl:attribute name="type">xsd:time</xsl:attribute>
			</xsl:when>
			<xsl:when test="$pItemDef/@DataType = 'datetime'">
				<xsl:attribute name="type">xsd:dateTime</xsl:attribute>
			</xsl:when>
			<xsl:when test="$pItemDef/@DataType = 'boolean'">
				<xsl:attribute name="type">xsd:boolean</xsl:attribute>
			</xsl:when>
			<xsl:when test="$pItemDef/@DataType = 'double'">
				<xsl:attribute name="type">xsd:decimal</xsl:attribute>
			</xsl:when>
			<xsl:when test="$pItemDef/@DataType = 'base64Binary'">
				<xsl:attribute name="type">xsd:base64Binary</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:attribute name="type">xsd:string</xsl:attribute>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Create Groups from OpenClinica Section Labels -->
	<xsl:template name="createGroups">

		<xsl:param name="pForm" />

		<xsl:variable name="vSections"
			select="//*[local-name()='ItemDef']/*/*/*[local-name()='SectionLabel'][generate-id() = generate-id(key('kLabelsInForm', concat($pForm/@OID, '+', .))[1])]" />

		<xsl:for-each select="$vSections">

			<xf:group>

				<xsl:variable name="vSection" select="." />
				
				<xsl:attribute name="id"><xsl:value-of select="position()" /></xsl:attribute>
				<xf:label><xsl:value-of select="//*[local-name()='ItemDef']/*/*/*[local-name()='SectionTitle' and ../OpenClinica:SectionLabel=$vSection][1]" /></xf:label>
		
				<!-- Add the subject key input field only to the first group. -->
				<xsl:if test="position()=1">
					<xf:input bind="subjectkey">
						<xf:label>Subject Key</xf:label>
						<xf:hint>The subject key for whom you are collecting data for.</xf:hint>
					</xf:input>
				</xsl:if>

				<xsl:for-each select="$pForm/odm:ItemGroupRef">

					<xsl:variable name="vItemGroupOID" select="@ItemGroupOID" />
					<xsl:variable name="vItemGroupDef" select="//*[local-name()='ItemGroupDef' and @OID=$vItemGroupOID and */*/@FormOID=$pForm/@OID]" />

					<xsl:apply-templates select="$vItemGroupDef">
						<xsl:with-param name="pForm" select="$pForm" />
						<xsl:with-param name="pSection" select="$vSection" />
						<xsl:with-param name="pItemGroupDef" select="$vItemGroupDef" />
					</xsl:apply-templates>

				</xsl:for-each>

			</xf:group>

		</xsl:for-each>

	</xsl:template>

	<!-- Create repeat questions -->
	<xsl:template match="//*[local-name()='ItemGroupDef' and @Repeating='Yes']">

		<xsl:param name="pForm" />
		<xsl:param name="pSection" />
		<xsl:param name="pItemGroupDef" />

		<xsl:if test="$pItemGroupDef/*/*/@SectionLabel=$pSection">
			<xf:group>
				<xsl:attribute name="id"><xsl:value-of select="$pItemGroupDef/@OID" /></xsl:attribute>
				<xf:label>
					<xsl:choose>
						<xsl:when test="$pItemGroupDef/*/*/*[local-name()='ItemGroupHeader']">
							<xsl:value-of select="$pItemGroupDef/*/*/*[local-name()='ItemGroupHeader']" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:if test="position()=1">
								<xsl:for-each select="$pItemGroupDef/odm:ItemRef">
									<xsl:variable name="vItemOID" select="@ItemOID" />
									<xsl:variable name="vItemDef" select="//*[local-name()='ItemDef' and @OID=$vItemOID]" />
									<xsl:if test="$vItemDef/*/*[@FormOID=$pForm/@OID]/*[local-name()='SectionLabel']=$pSection">
										<xsl:value-of select="$vItemDef/@Comment" />
									</xsl:if>
								</xsl:for-each>
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
				</xf:label>

				<xf:repeat>

					<xsl:attribute name="bind"><xsl:value-of select="$pItemGroupDef/@OID" /></xsl:attribute>

					<xsl:for-each select="$pItemGroupDef/odm:ItemRef">

						<xsl:variable name="vItemOID" select="@ItemOID" />
						<xsl:variable name="vItemDef" select="//*[local-name()='ItemDef' and @OID=$vItemOID][1]" />

						<xsl:if test="$vItemDef/*/*[@FormOID=$pForm/@OID]/*[local-name()='SectionLabel']=$pSection">
							<xsl:apply-templates select=".">
								<xsl:with-param name="pItemDef" select="$vItemDef" />
							</xsl:apply-templates>
						</xsl:if>
					</xsl:for-each>
				</xf:repeat>
			</xf:group>
		</xsl:if>
	</xsl:template>

	<!-- Create non-repeat questions -->
	<xsl:template match="//*[local-name()='ItemGroupDef' and @Repeating='No']">

		<xsl:param name="pForm" />
		<xsl:param name="pSection" />
		<xsl:param name="pItemGroupDef" />

		<xsl:for-each select="$pItemGroupDef/odm:ItemRef">

			<xsl:variable name="vItemOID" select="@ItemOID" />
			<xsl:variable name="vItemDef" select="//*[local-name()='ItemDef' and @OID=$vItemOID][1]" />

			<xsl:if test="$vItemDef/*/*[@FormOID=$pForm/@OID]/*[local-name()='SectionLabel']=$pSection">
				<xsl:apply-templates select=".">
					<xsl:with-param name="pForm" select="$pForm"></xsl:with-param>
					<xsl:with-param name="pItemDef" select="$vItemDef" />
				</xsl:apply-templates>
			</xsl:if>
		</xsl:for-each>

	</xsl:template>

	<!-- Create either select or input type questions. -->
	<xsl:template match="//*[local-name()='ItemRef']">

		<xsl:param name="pItemDef" />

		<xsl:choose>
			<xsl:when test="$pItemDef/odm:CodeListRef or $pItemDef/*[local-name()='MultiSelectListRef']">
				<xsl:apply-templates select="$pItemDef" mode="createSelectQuestions">
					<xsl:with-param name="pItemDef" select="$pItemDef" />
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="$pItemDef" mode="createInputQuestions">
					<xsl:with-param name="pItemDef" select="$pItemDef" />
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>

	<xsl:template match="//*[local-name()='ItemDef']" mode="createInputQuestions">

		<xsl:param name="pItemDef" />

		<xsl:call-template name="createHeaderInfo">
			<xsl:with-param name="pItemDef" select="$pItemDef" />
		</xsl:call-template>

		<xf:input>
			<xsl:attribute name="bind"><xsl:value-of select="$pItemDef/@OID" /></xsl:attribute>
			<xf:label>
				<xsl:variable name="vLText">
					<xsl:apply-templates select="$pItemDef">
						<xsl:with-param name="pItemDef" select="$pItemDef" />
					</xsl:apply-templates>
				</xsl:variable>
				<xsl:value-of select="normalize-space($vLText)" />
			</xf:label>
			<hint>
				<xsl:apply-templates select="$pItemDef" mode="createHintText">
					<xsl:with-param name="pItemDef" select="$pItemDef" />
				</xsl:apply-templates>
			</hint>
		</xf:input>
	</xsl:template>

	<!-- Create select questions -->
	<xsl:template match="//*[local-name()='ItemDef']" mode="createSelectQuestions">
		<xsl:param name="pItemDef" />
		<xsl:choose>
			<xsl:when test="$pItemDef/odm:CodeListRef">
				<xsl:apply-templates select="$pItemDef/*[local-name()='CodeListRef']">
					<xsl:with-param name="pItemDef" select="$pItemDef" />
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise test="$pItemDef/*[local-name()='MultiSelectListRef']">
				<xsl:apply-templates select="$pItemDef/*[local-name()='MultiSelectListRef']">
					<xsl:with-param name="pItemDef" select="$pItemDef" />
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- Create single select questions -->
	<xsl:template match="//*[local-name()='ItemDef']/*[local-name()='CodeListRef']">

		<xsl:param name="pItemDef" />

		<xsl:call-template name="createHeaderInfo">
			<xsl:with-param name="pItemDef" select="$pItemDef" />
		</xsl:call-template>

		<xf:select1>
			<xsl:attribute name="bind"><xsl:value-of select="$pItemDef/@OID" /></xsl:attribute>
			<xsl:variable name="codeListID" select="$pItemDef/odm:CodeListRef/@CodeListOID" />
			<xf:label>
				<xsl:variable name="lText">
					<xsl:apply-templates select="$pItemDef">
						<xsl:with-param name="pItemDef" select="$pItemDef" />
					</xsl:apply-templates>
				</xsl:variable>

				<xsl:value-of select="normalize-space($lText)" />
			</xf:label>
			<xsl:for-each select="../../odm:CodeList[@OID = $codeListID]/odm:CodeListItem">
				<xf:item>
					<xsl:attribute name="id"><xsl:value-of select="@CodedValue" /></xsl:attribute>
					<xf:label><xsl:value-of select="odm:Decode/odm:TranslatedText" /></xf:label>
					<xf:value><xsl:value-of select="@CodedValue" /></xf:value>
				</xf:item>
			</xsl:for-each>
			<xf:hint>
				<xsl:apply-templates select="$pItemDef" mode="createHintText">
					<xsl:with-param name="pItemDef" select="$pItemDef" />
				</xsl:apply-templates>
			</xf:hint>
		</xf:select1>
	</xsl:template>

	<!-- Create multi select questions -->
	<xsl:template match="//*[local-name()='ItemDef']/*[local-name()='MultiSelectListRef']">
		<xsl:param name="pItemDef" />
		
		<xsl:call-template name="createHeaderInfo">
			<xsl:with-param name="pItemDef" select="$pItemDef" />
		</xsl:call-template>
		<xf:select>
			<xsl:variable name="vMultiSelectListId" select="$pItemDef/*[local-name()='MultiSelectListRef']/@MultiSelectListID" />
			<xsl:attribute name="bind"><xsl:value-of select="$pItemDef/@OID" /></xsl:attribute>
			<xf:label>
				<xsl:variable name="lText">
					<xsl:apply-templates select="$pItemDef">
						<xsl:with-param name="pItemDef" select="$pItemDef" />
					</xsl:apply-templates>
				</xsl:variable>
		
				<xsl:value-of select="normalize-space($lText)" />
			</xf:label>
			<xsl:for-each select="//*[local-name()='MultiSelectList' and @ID=$vMultiSelectListId]/*[local-name()='MultiSelectListItem']">
				<xf:item>
					<xsl:attribute name="id"><xsl:value-of select="./*/*[local-name()='TranslatedText']" /></xsl:attribute>
					<xf:label><xsl:value-of select="./*/*[local-name()='TranslatedText']"></xsl:value-of></xf:label>
					<xf:value><xsl:value-of select="@CodedOptionValue"></xsl:value-of></xf:value>
				</xf:item>
			</xsl:for-each>
			<xf:hint>
				<xsl:apply-templates select="$pItemDef" mode="createHintText">
					<xsl:with-param name="pItemDef" select="$pItemDef" />
				</xsl:apply-templates>
			</xf:hint>
		</xf:select>
	</xsl:template>

	<xsl:template name="createHeaderInfo">
		<xsl:param name="pItemDef" />

		<xsl:if
			test="$pItemDef/*/*[local-name()='ItemPresentInForm' and position()=1]/*[local-name()='ItemHeader'] and $pItemDef/*/*[local-name()='ItemPresentInForm' and position()=1]/*[local-name()='ItemSubHeader']">
			<xf:input>
				<xsl:attribute name="bind"><xsl:value-of select="$pItemDef/@OID" />_HEADER</xsl:attribute>
				<xf:label>|-<xsl:value-of select="normalize-space($pItemDef/*/*/*[local-name()='ItemHeader'])" /></xf:label>
				<xf:hint>This question is a header label for the next question. Do not ANSWER</xf:hint>
			</xf:input>
		</xsl:if>

		<xsl:if test="$pItemDef/*/*[local-name()='ItemPresentInForm' and position()=1]/*[local-name()='ItemSubHeader']">

			<xf:input>
				<xsl:attribute name="bind"><xsl:value-of select="$pItemDef/@OID" />_SUB_HEADER</xsl:attribute>
				<xf:label>|--<xsl:value-of select="normalize-space($pItemDef/*/*/*[local-name()='ItemSubHeader'])" /></xf:label>
				<xf:hint>This question is a sub-header label for the next question. Do not ANSWER</xf:hint>
			</xf:input>

		</xsl:if>
	</xsl:template>

	<!-- Append question number to the label text if it exists, else use translated text -->
	<xsl:template match="//*[local-name()='ItemDef']">

		<xsl:param name="pItemDef" />

		<xsl:choose>
			<xsl:when test="$pItemDef/odm:Question/@OpenClinica:QuestionNumber">
				<xsl:value-of select="normalize-space($pItemDef/odm:Question/@OpenClinica:QuestionNumber)" />
				<xsl:value-of select="normalize-space($pItemDef/odm:Question/odm:TranslatedText)" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="normalize-space($pItemDef/odm:Question/odm:TranslatedText)" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="//*[local-name()='ItemDef']" mode="createHintText">

		<xsl:param name="pItemDef" />

		<xsl:choose>
			<xsl:when test="$pItemDef/odm:MeasurementUnitRef">
				<xsl:variable name="vMeasurementUnitOID">
					<xsl:value-of select="$pItemDef/odm:MeasurementUnitRef/@MeasurementUnitOID" />
				</xsl:variable>
				<xsl:value-of select="//odm:MeasurementUnit[@OID=$vMeasurementUnitOID]/odm:Symbol/odm:TranslatedText" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$pItemDef/@Comment" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
