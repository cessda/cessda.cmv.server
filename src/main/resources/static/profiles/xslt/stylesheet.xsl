<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:pr="ddi:ddiprofile:3_2"
				xmlns:r="ddi:reusable:3_2" exclude-result-prefixes="xs" version="2.0">
	<xsl:output method="html" encoding="utf-8" indent="yes"/>
	<xsl:template match="/">
		<html lang="en">
			<head>
				<meta charset="utf-8"/>
				<meta name="viewport" content="width=device-width, initial-scale=1"/>
				<link href="data:image/x-icon;base64,AAABAAEAEBAAAAEAIABoBAAAFgAAACgAAAAQAAAAIAAAAAEAIAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD6xoP/+saD//rGg//6xoP/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD6xoP/+saD//rGg//6xoP/+saD//rGg/8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADztFX/+saD//rGg//6xoP/+saD//rGg//6xoP/+saD/wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADxrkb/8a5G//GuRv/6xoP////////////6xoP/+saD//rGg//6xoP/AAAAAAAAAAAAAAAAAAAAAAAAAADxrkb/8a5G//GuRv/xrkb///////////////////////rGg//6xoP/+saD//rGg/8AAAAAAAAAAAAAAADxrkb/8a5G//GuRv/xrkb/////////////////////////////////+saD//rGg//6xoP/+saD/wAAAADxrkb/8a5G//GuRv/xrkb////////////////////////////////////////////6xoP/+saD//rGg//6xoP/8a5G//GuRv/xrkb//////////////////////01NTf9NTU3///////////////////////rGg//6xoP/+saD//GuRv/xrkb/8a5G//////////////////////9NTU3/TU1N///////////////////////6xoP/+saD//rGg//xrkbN8a5G//GuRv/xrkb////////////////////////////////////////////6xoP/+saD//rGg/99Y0I1AAAAAPGuRv/xrkb/8a5G//GuRv/////////////////////////////////6xoP/+saD//rGg//6xoP/AAAAAAAAAAAAAAAA8a5G//GuRv/xrkb/8a5G///////////////////////6xoP/+saD//rGg//6xoP/AAAAAAAAAAAAAAAAAAAAAAAAAADxrkb/8a5G//GuRv/xrkb////////////xrkb/+saD//rGg//6xoP/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAPGuRv/xrkb/8a5G//GuRv/xrkb/8a5G//GuRv/xrkb/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA8a5G//GuRv/xrkb/8a5G//GuRv/xrkb/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA8a5G//GuRv8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA/D8AAPgfAADwDwAA4AcAAMADAACAAQAAAAAAAAAAAAAAAAAAAAEAAIABAADAAwAA4AcAAPAPAAD4HwAA/n8AAA=="
					  rel="icon" type="image/x-icon"/>
				<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css" rel="stylesheet"
					  integrity="sha384-KyZXEAg3QhqLMpG8r+8fhAXLRk2vvoC2f3B09zVXn8CA5QIVfZOJ3BCsw2P0p/We"
					  crossorigin="anonymous"/>
				<style>
					body, td, th {
					font-size:13px;
					color:#333;
					}
					td.wrap {
					overflow-wrap: break-word;
					word-wrap: break-word;
					-ms-word-break: break-all;
					word-break: break-all;
					word-break: break-word;
					}
					h1 {
					font-size:24px;
					line-height: 34px;
					}
					.xpath {
					font-size:12px;
					font-weight: bold;
					width:30%;
					}
					.usage {
					width:33%;
					}
					#home svg {
					width:140px;
					height: auto;
					}
					#home {
					display: block;
					width: 140px;

					}
					#footerlogo {
					width:100px;
					height: auto;
					}
					.bg-grey-dk-200 {
					background-color: #4e4d56 !important;
					}
					.bg-grey-dk-200 a {
					display: inline-block;
					color: #fff;
					line-height: 30px;
					padding: 0 15px;
					text-decoration: none;
					background: transparent;
					transition: all 0.3s;
					}
					.bg-grey-dk-200 a:hover {

					background: rgba(255,255,255,0.1);

					}
				</style>
				<title>
					<xsl:value-of select="//pr:DDIProfileName/r:String"/>
				</title>
			</head>
			<body>
				<header class="bg-light mb-4">
					<div class="container-xxl">
						<div class="row py-4">
							<div class="col">
								<a id="home" target="_blank" href="https://cessda.eu">
									<svg viewBox="0 0 2386 652" xmlns="http://www.w3.org/2000/svg" fill-rule="evenodd"
										 clip-rule="evenodd" stroke-linejoin="round" stroke-miterlimit="2"
										 aria-label="CESSDA Home Page">
										<path d="M993.333 498.333h-92.5c-52.5 0-95-42.5-95-94.583V249.167c0-52.084 42.5-94.584 95-94.584l92.5-.416c9.584 0 17.5 7.916 17.5 17.5 0 9.583-7.916 17.5-17.5 17.916l-92.5.417c-32.916 0-59.583 26.667-59.583 59.583v154.584c0 32.916 26.667 59.583 59.583 59.583h92.5c9.584 0 17.5 7.917 17.5 17.5.417 9.167-7.5 17.083-17.5 17.083"
											  fill="#595959" fill-rule="nonzero"></path>
										<path d="M1254.17 498.75h-109.584c-52.5 0-95-42.5-95-95V249.583c0-52.5 42.5-95 95-95h44.167c52.5 0 95 42.5 95 95v85.834h-165c-9.583 0-17.5-7.917-17.5-17.5 0-9.584 7.917-17.5 17.5-17.5h130v-50.834c0-32.916-26.667-59.583-59.583-59.583h-44.167c-32.917 0-59.583 26.667-59.583 59.583v154.584c0 32.916 26.666 59.583 59.583 59.583h109.167c9.583 0 17.5 7.917 17.5 17.5s-7.917 17.5-17.5 17.5M2290.83 497.083h-54.583c-26.25 0-50.833-12.916-68.75-36.25-17.083-22.083-26.25-51.25-26.25-82.083 0-62.5 39.167-106.25 95-106.25h77.917c9.583 0 17.5 7.917 17.5 17.5s-7.917 17.5-17.5 17.5h-77.917c-35.833 0-59.583 28.333-59.583 70.833 0 45.834 26.666 82.917 59.583 82.917h54.583c32.917 0 59.584-26.667 59.584-59.583V247.5c0-32.917-26.667-59.583-59.584-59.583h-102.083c-9.583 0-17.5-7.917-17.5-17.5 0-9.584 7.917-17.5 17.5-17.5h102.083c52.5 0 95 42.5 95 95V402.5c0 52.083-42.5 94.583-95 94.583M1997.92 498.75h-55.834c-52.5 0-95-42.5-95-95V249.167c0-52.5 42.5-95 95-95h79.167c9.583 0 17.5 7.916 17.5 17.5 0 9.583-7.917 17.5-17.5 17.5h-79.167c-32.916 0-59.583 26.666-59.583 59.583v154.583c0 32.917 26.667 59.584 59.583 59.584h55.834c32.916 0 59.583-26.667 59.583-59.584V17.5c0-9.583 7.917-17.5 17.5-17.5s17.5 7.917 17.5 17.5v386.25c0 52.5-42.5 95-94.583 95M1457.08 498.75h-101.25c-9.583 0-17.5-7.917-17.5-17.5s7.917-17.5 17.5-17.5h101.25c31.667 0 57.5-25.833 57.5-57.5 0-27.083-19.166-50.833-45.833-56.25l-49.583-10.417c-7.917-1.25-19.167-4.583-22.5-5.833-35.834-13.75-59.584-48.333-59.584-86.667 0-51.25 41.667-92.5 92.5-92.5h76.25c9.584 0 17.5 7.917 17.5 17.5 0 9.584-7.916 17.5-17.5 17.5h-76.25c-31.666 0-57.5 25.834-57.5 57.5 0 23.75 15 45 36.667 53.75 1.667.834 10.833 3.334 16.25 4.167h.417l49.583 10.417c43.75 8.75 75 47.083 75 90.833 0 50.833-41.667 92.5-92.917 92.5M1704.17 498.75h-101.25c-9.584 0-17.5-7.917-17.5-17.5s7.916-17.5 17.5-17.5h101.25c31.666 0 57.5-25.833 57.5-57.5 0-27.083-19.167-50.833-45.834-56.25l-49.583-10.417c-7.917-1.25-19.167-4.583-22.5-5.833-35.833-13.75-59.583-48.333-59.583-86.667 0-51.25 41.666-92.5 92.5-92.5h76.25c9.583 0 17.5 7.917 17.5 17.5 0 9.584-7.917 17.5-17.5 17.5h-76.25c-31.667 0-57.5 25.834-57.5 57.5 0 23.75 15 45 37.083 53.75 1.667.834 10.833 3.334 16.25 4.167h.417l49.583 10.417c42.917 8.75 74.167 47.083 74.167 90.833.416 50.833-41.25 92.5-92.5 92.5"
											  fill="#4d4d4d" fill-rule="nonzero"></path>
										<circle cx="325.417" cy="323.333" r="54.167" fill="#4d4d4d"></circle>
										<path d="M537.5 143.75l-95.417-95C416.25 22.5 383.333 6.25 347.5 1.667 340 .417 332.5 0 325 0c-44.583 0-86.25 17.083-117.5 48.333L48.75 207.5C17.5 238.75 0 280.417 0 325s17.083 86.25 48.75 117.5l95.417 95.833c10 10 23.333 15.417 37.5 15.417 14.166 0 27.5-5.417 37.5-15.417S235 515 235 500.417c0-14.167-5.417-27.917-15.417-37.917l-95-95.833c-11.25-11.25-17.5-25.834-17.5-41.667 0-15.833 5.834-30.417 17.084-41.667l159.166-159.166c11.25-11.25 25.834-17.084 41.667-17.084 15.833 0 30.417 6.25 41.667 17.084l95.416 95c10 10 23.75 15.833 37.917 15.833 14.167 0 27.917-5.417 37.917-15.833 10-10 15.833-23.75 15.833-37.917-.417-13.75-5.833-27.5-16.25-37.5z"
											  fill="#46aef1" fill-rule="nonzero"></path>
										<path d="M602.917 209.583L507.5 113.75c-10-10-23.333-15.417-37.5-15.417-14.167 0-27.5 5.417-37.5 15.417s-15.833 23.333-15.833 37.917c0 14.166 5.416 27.916 15.416 37.916l95 95.834c11.25 11.25 17.5 25.833 17.5 41.666 0 15.834-5.833 30.417-17.083 41.667L368.333 527.917C357.083 539.167 342.5 545 326.667 545c-15.834 0-30.417-6.25-41.667-17.083l-95.417-95c-10-10-23.75-15.834-37.916-15.834-14.167 0-27.917 5.417-37.917 15.834-10 10-15.833 23.75-15.833 37.916 0 14.167 5.416 27.917 15.833 37.917l95.417 95c25.833 25.833 58.75 42.083 95 47.083 7.5.834 15 1.667 22.5 1.667 44.583 0 85.833-17.083 117.5-48.333L603.333 445c31.25-31.25 48.334-72.917 48.334-117.5-.417-45-17.5-86.667-48.75-117.917z"
											  fill="#83c6fa" fill-rule="nonzero"></path>
										<path d="M181.667 553.333c-2.5 0-5 0-7.084-.416-11.666-1.667-22.5-6.667-30.833-15l-95.417-95C17.5 411.25 0 369.583 0 325s17.083-86.25 48.333-117.5l75.834-75.833 74.583 77.083-74.583 74.583c-11.25 11.25-17.084 25.834-17.084 41.667 0 15.833 6.25 30.417 17.084 41.667l95.416 95c10 10 15.834 23.75 15.834 37.916 0 14.167-5.417 27.917-15.834 37.917-10 10.417-23.333 15.833-37.916 15.833z"
											  fill="#46aef1" fill-rule="nonzero"></path>
									</svg>
								</a>
							</div>
							<div class="col-10">

								<h1>
									<xsl:value-of select="//pr:DDIProfileName/r:String"/>
								</h1>
							</div>
						</div>
					</div>
				</header>
				<div class="container-xxl">
					<div class="row">
						<div class="col mt-2">
							<table class="table table-sm table-striped">
								<thead>
									<tr>
										<th class="xpath">
											DDI_XPath
										</th>
										<th>
											Required
										</th>
										<th>
											UI Label
										</th>
										<th>
											Type
										</th>
										<th>
											Repeatable
										</th>
										<th>
											Usage note
										</th>
									</tr>
								</thead>
								<xsl:for-each select="//pr:Used/r:Description">
									<xsl:choose>
										<xsl:when test="current()/r:Content[contains(text(),'Structural')]">
											<tr>
												<td class="xpath wrap">
													<xsl:value-of
															select="parent::node()/@xpath"
													/>
												</td>
												<td class="wrap">
													<xsl:value-of
															select="substring-after(current()/r:Content[starts-with(text(),'Required')],'Required: ')"
													/>
												</td>
												<td class="text-danger">
													<xsl:value-of
															select="substring-after(current()/r:Content[starts-with(text(),'CDC')],'CDC_UI_Label: ')"
													/>
													<xsl:value-of
															select="substring-after(current()/r:Content[starts-with(text(),'EQB_UI')],'EQB_UI_Label: ')"
													/>
												</td>
												<td>
													<xsl:value-of
															select="substring-after(current()/r:Content[starts-with(text(),'ElementType')],'ElementType: ')"
													/>
												</td>
												<td>
													<xsl:value-of
															select="substring-after(current()/r:Content[starts-with(text(),'ElementRepeatable')],'ElementRepeatable: ')"
													/>
												</td>
												<td class="usage wrap">
													<xsl:value-of
															select="substring-after(current()/r:Content[starts-with(text(),'Usage')],'Usage: ')"
													/>
												</td>
											</tr>
										</xsl:when>
										<xsl:otherwise>
											<tr>
												<td class="xpath wrap">


													<xsl:value-of
															select="parent::node()/@xpath"
													/>
												</td>
												<td class="wrap">
													<xsl:value-of
															select="substring-after(current()/r:Content[starts-with(text(),'Required')],'Required: ')"
													/>
												</td>
												<td class="text-danger">
													<xsl:value-of
															select="substring-after(current()/r:Content[starts-with(text(),'CDC')],'CDC_UI_Label: ')"
													/>
													<xsl:value-of
															select="substring-after(current()/r:Content[starts-with(text(),'EQB_UI')],'EQB_UI_Label: ')"
													/>
												</td>
												<td>
													<xsl:value-of
															select="substring-after(current()/r:Content[starts-with(text(),'ElementType')],'ElementType: ')"
													/>
												</td>
												<td>
													<xsl:value-of
															select="substring-after(current()/r:Content[starts-with(text(),'ElementRepeatable')],'ElementRepeatable: ')"
													/>
												</td>
												<td class="usage wrap">
													<xsl:value-of
															select="substring-after(current()/r:Content[starts-with(text(),'Usage')],'Usage: ')"
													/>
												</td>
											</tr>
										</xsl:otherwise>
									</xsl:choose>

								</xsl:for-each>
							</table>

						</div>
					</div>
				</div>
				<footer class="bg-grey-dk-200 py-2">
					<div class="container-xxl">
						<div class="row">
							<div class="col">

								<svg id="footerlogo" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 189 56">
									<style>.ssst0{fill:#fff}</style>
									<path class="ssst0"
										  d="M79.29 41.35h-7.05c-3.99 0-7.24-3.24-7.24-7.23V22.34c0-3.99 3.25-7.23 7.24-7.23l7.05-.03h.01c.74 0 1.34.6 1.35 1.34 0 .74-.6 1.35-1.34 1.35l-7.05.03c-2.51 0-4.55 2.03-4.55 4.53v11.78c0 2.5 2.04 4.54 4.55 4.54h7.05c.74 0 1.35.6 1.35 1.35-.03.74-.63 1.35-1.37 1.35M99.17 41.37H90.8c-3.99 0-7.24-3.25-7.24-7.24V22.34c0-3.99 3.25-7.24 7.24-7.24h3.37c3.99 0 7.24 3.25 7.24 7.24v6.55h-12.6a1.35 1.35 0 0 1 0-2.7h9.9v-3.85c0-2.51-2.04-4.54-4.55-4.54H90.8c-2.51 0-4.54 2.04-4.54 4.54v11.79c0 2.51 2.04 4.54 4.54 4.54h8.37c.74 0 1.35.6 1.35 1.35-.01.75-.61 1.35-1.35 1.35M178.26 41.23h-4.17c-2 0-3.86-.98-5.23-2.76-1.29-1.68-2.01-3.9-2.01-6.26 0-4.78 2.98-8.11 7.24-8.11h5.93a1.35 1.35 0 0 1 0 2.7h-5.93c-2.72 0-4.55 2.18-4.55 5.42 0 3.49 2.04 6.33 4.55 6.33h4.17c2.51 0 4.55-2.04 4.55-4.54V22.2c0-2.5-2.04-4.54-4.55-4.54h-7.77a1.35 1.35 0 0 1 0-2.7h7.77c3.99 0 7.24 3.25 7.24 7.24V34c0 3.99-3.25 7.23-7.24 7.23M155.9 41.37h-4.26c-3.99 0-7.24-3.25-7.24-7.24v-11.8c0-3.99 3.25-7.24 7.24-7.24h6.04a1.35 1.35 0 0 1 0 2.7h-6.04c-2.51 0-4.55 2.04-4.55 4.54v11.8c0 2.51 2.04 4.54 4.55 4.54h4.26c2.51 0 4.55-2.04 4.55-4.54V4.66a1.35 1.35 0 0 1 2.7 0v29.47c-.01 3.99-3.26 7.24-7.25 7.24M114.66 41.37h-7.72a1.35 1.35 0 0 1 0-2.7h7.72c2.41 0 4.38-1.96 4.38-4.37 0-2.07-1.47-3.87-3.5-4.29l-3.77-.78c-.6-.11-1.46-.36-1.72-.46-2.72-1.04-4.55-3.69-4.55-6.6 0-3.9 3.17-7.07 7.07-7.07h5.83a1.35 1.35 0 0 1 0 2.7h-5.83c-2.41 0-4.38 1.96-4.38 4.37 0 1.8 1.13 3.44 2.81 4.09.13.05.81.25 1.25.33l.04.01 3.79.79c3.27.67 5.65 3.58 5.65 6.92 0 3.89-3.17 7.06-7.07 7.06M133.51 41.37h-7.72a1.35 1.35 0 0 1 0-2.7h7.72c2.41 0 4.38-1.96 4.38-4.37 0-2.07-1.47-3.87-3.5-4.29l-3.77-.78c-.6-.11-1.45-.36-1.72-.46-2.72-1.04-4.55-3.69-4.55-6.6 0-3.9 3.17-7.07 7.07-7.07h5.83a1.35 1.35 0 0 1 0 2.7h-5.83c-2.41 0-4.38 1.96-4.38 4.37 0 1.8 1.13 3.44 2.81 4.09.13.05.81.25 1.25.33l.04.01 3.79.79c3.27.67 5.65 3.58 5.65 6.92 0 3.89-3.17 7.06-7.07 7.06M32.49 27.99c0 2.29-1.85 4.14-4.14 4.14-2.29 0-4.14-1.85-4.14-4.14 0-2.29 1.85-4.14 4.14-4.14 2.28 0 4.14 1.85 4.14 4.14"></path>
									<path class="ssst0"
										  d="M35.62 11.38c.91-.9 2.12-1.4 3.41-1.4.61 0 1.2.12 1.75.33L37.26 6.8c-2.02-2.01-4.56-3.28-7.37-3.65-.58-.08-1.16-.11-1.73-.11-3.44 0-6.67 1.34-9.09 3.76L7.14 18.73c-.3.3-.59.62-.86.95-1.89 2.29-2.91 5.15-2.91 8.16 0 3.44 1.34 6.67 3.77 9.1l2.88 2.88.61.61v-.01l3.4 3.4c.45.45.98.77 1.55.99l.04.04c.06.02.13.03.19.05.06.02.11.03.17.05.19.05.39.1.59.13h.05c.18.02.35.05.53.05 1.18 0 2.29-.46 3.12-1.28l.01-.01c.01-.01.02-.01.02-.02.84-.84 1.3-1.95 1.3-3.13s-.46-2.3-1.3-3.13l-3.85-3.85-3.03-3.05a3.995 3.995 0 0 1-1.18-2.84c0-.83.25-1.62.72-2.29.13-.18.27-.36.43-.53l5.88-5.88 6.06-6.07c.75-.75 1.76-1.17 2.83-1.17s2.08.42 2.84 1.17l3.53 3.52c-.22-.56-.34-1.16-.33-1.78-.01-1.28.5-2.49 1.42-3.41"></path>
									<path class="ssst0"
										  d="M49.29 18.87l-6.94-6.99a4.404 4.404 0 0 0-3.11-1.27c-1.18 0-2.29.46-3.12 1.28-.84.83-1.31 1.95-1.31 3.13-.01 1.18.45 2.3 1.29 3.14l6.92 6.97c.76.76 1.18 1.77 1.18 2.84 0 1.07-.41 2.08-1.16 2.83L31.1 42.73c-.75.75-1.76 1.17-2.83 1.17s-2.08-.42-2.84-1.17l-3.49-3.49c.2.54.32 1.12.32 1.71 0 1.29-.5 2.51-1.42 3.43-.91.92-2.13 1.42-3.43 1.42a4.603 4.603 0 0 1-1.82-.36l3.57 3.57c2.02 2.01 4.56 3.28 7.37 3.65.57.08 1.15.11 1.73.11 3.44 0 6.67-1.33 9.09-3.76L49.3 37.06c2.42-2.43 3.76-5.66 3.76-9.1s-1.35-6.67-3.77-9.09"></path>
								</svg>
							</div>

							<div class="col-10 text-end">
								<a href="https://www.cessda.eu/Privacy-policy">Privacy Policy</a>
								<a href="https://www.cessda.eu/Acceptable-Use-Policy">Acceptable Use Policy</a>
								<a href="https://www.cessda.eu/Tools-Services">CESSDA Tools &amp; Services</a>

							</div>
						</div>
					</div>

				</footer>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
