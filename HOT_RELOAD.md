
// static interfaces, dynamic implementation
String rendered = views.index
        .template("ValueA")
        .render()
        .toString();

// dynamic interfaces & implementation
String rendered = Rocker("views/index.rocker.html")
        .bind("val", "ValueA")
        .render()
        .toString();

// ninja
Result result = Results
        .template("views/index.rocker.html")
        .render("val", "ValueA");


