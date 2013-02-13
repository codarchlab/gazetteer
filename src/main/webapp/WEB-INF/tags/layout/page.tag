<%@ tag description="page layout" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="title" required="true" type="java.lang.String"%>
<%@ attribute name="subtitle" type="java.lang.String"%>

<s:url var="searchAction" value="/app/#search" />

<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>iDAI.gazetteer - ${title}</title>
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
<script type="text/javascript" src='http://maps.google.com/maps/api/js?sensor=false'></script>
<script src="/gazetteer/resources/js/custom.js"></script>
<script src="/gazetteer/widget/lib.js"></script>
<script src='/gazetteer/resources/js/lib/jquery.locationpicker.js'></script>
<script src='/gazetteer/resources/js/lib/jquery.jstree.js'></script>
<script src='/gazetteer/resources/bootstrap/js/bootstrap.min.js'></script>
<link href="/gazetteer/resources/bootstrap/css/bootstrap.css" rel="stylesheet">
<link href="/gazetteer/resources/bootstrap/css/font-awesome.css" rel="stylesheet">	
<link href="/gazetteer/resources/css/app.css" rel="stylesheet">
<style type="text/css">
      body {
        padding-top: 60px;
        padding-bottom: 40px;
      }
    </style>
</head>
<body>

<!-- Top Navigation Bar -->
<div class="navbar navbar-fixed-top navbar-inverse">
	<div class="navbar-inner">
		<div class="container-fluid">
			<a class="btn btn-navbar" data-toggle="collapse"
				data-target=".nav-collapse"> <span class="icon-bar"></span> <span
				class="icon-bar"></span> <span class="icon-bar"></span>
			</a> <a class="brand" href="/gazetteer">iDAI.gazetteer</a>
			<div class="btn-group pull-right">
				<a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
					<i class="icon-user"></i> Username <span class="caret"></span>
				</a>
				<ul class="dropdown-menu">
					<li><a href="#">Profile</a></li>
					<li class="divider"></li>
					<li><a href="#">Sign Out</a></li>
				</ul>
			</div>
			<div class="nav-collapse">
				<ul class="nav">
					<li><a href="/gazetteer/thesaurus"><s:message code="ui.thesaurus.list" text="ui.thesaurus.list"/></a></li>
					<li>
						<a href="/gazetteer/app/#/extended-search">
							<s:message code="ui.search.extendedSearch" text="Erweiterte Suche"/>
						</a>
					</li>
				</ul>
			</div><!--/.nav-collapse -->
			<form:form class="navbar-search pull-left simpleSearchForm" action="${searchAction}" method="GET">
				<s:message code="ui.search.simpleSearch" text="Einfache Suche" var="titleSimpleSearch"/>
 				<input type="text" class="search-query" placeholder="${titleSimpleSearch}" name="q">
 				<i class="icon-search icon-white"></i>
			</form:form>
		</div>
	</div>
</div>

<div class="container-fluid">

	<!-- Page title -->
	<div class="page-header">
		<h1>
			${title}
			<small>${subtitle}</small>
		</h1>
	</div>

	<jsp:doBody />
	
	<!-- Footer -->
	<hr>
	<footer>
		<jsp:useBean id="now" class="java.util.Date" />
		<fmt:formatDate var="year" value="${now}" pattern="yyyy" />
		<p>&copy; Deutsches Archäologisches Institut ${year}</p>
	</footer>
	
</div>

</body>
</html>