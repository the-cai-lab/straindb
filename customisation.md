# Customising

This document will guide you through the steps to customise the application after you've cloned it from the repository.

## Define Initials and Prefixes

You can define the lab initials and prefixes for each catalogue type in the application.
To do this, navigate to the `src/main/java/database/cailab/org/website/service/ChemLabConsts.java` file and modify the following constants:

```java
    LAB_INITIALS = "";
    BACTERIA_PREFIX = "";
    PRIMERS_PREFIX = "";
    YEAST_PREFIX = "";
    MAMMALIAN_PREFIX = "";
    PLANTS_PREFIX = "";
```    

Variable Details:
- `LAB_INITIALS`: The lab's abbreviation
- `BACTERIA_PREFIX`: Prefix for bacteria entries
- `PRIMERS_PREFIX`: Prefix for primers entries
- `YEAST_PREFIX`: Prefix for yeast entries
- `MAMMALIAN_PREFIX`: Prefix for mammalian entries
- `PLANTS_PREFIX`: Prefix for plants entries

## Update Project Group and Description

You can update the project's group ID and description by editing the pom.xml file.

Open the pom.xml file in the root directory of the project and locate the following tags:

```xml
<groupId></groupId>
<description></description>
```

`<groupId>`: Represents your project's group or organisation name. You can change this to match your company's domain or naming convention.

`<description>`: A short summary of what the application does.

## Change Login Page Title, Browser Tab Title, and Text Logo

You can customise the following elements of the application's UI:
- The login page title (large text displayed on the login screen)
- The browser tab title (shown in the browser tab)
- The text logo (displayed at the top-right corner of each page)

1. Update Login Page Title (on-screen text)
Open: `src/main/resources/templates/login/login.html`, find this line of code:

```html
<span class="d-sm-inline sidenavfont sidenavtitle" style="font-size: 60px;">...</span>
```

Add your desired text between the <span></span> tags

2. Change Browser Tab Title and Top-Right Text Logo
Open `src/main/java/database/cailab/org/website/service/ChemLabConsts.java`

Locate and update the following variables:

```java
    PAGE_TITLE = "";
    PAGE_TEXT_LOGO = "";
```

Variable Details:
- `PAGE_TITLE` : Sets the title shown on the browser tab.
- `PAGE_TEXT_LOGO`: Sets the text logo shown in the top-right corner of each page. (You can leave it blank if you do not want to display a text logo.)

## Map the Base URL to Your Domain

To ensure the application correctly aligns with your actual deployment domain, you need to update the base URL in the configuration files.
Open `src/main/resources/application-prod.properties`, find the following property and update it with your actual domain name:

```properties
    application.domain = 
```

This setting is also available in other environment-specific property files:
- `application-dev.properties`
- `application-test.properties`
update those files as needed. 

## Update the "About" Page

The About page provides general information about the application. It was written in simple HTML format and can be customised to match your organisational details.

Open `/src/main/resources/templates/about/about.html` , then edit the HTML content as needed using standard HTML syntax.

## Link to Your Parent Site

You can add or update a navigation link that points to your organisation's parent website.

Open `/src/main/resources/templates/fragments/homeleftnav.html`, then locate the following code block:

```html
<li class="nav-item">
    <a th:href="@{https://www.google.com/}" class="nav-link align-middle px-2">
        <span class="fs-4 d-block d-sm-inline sidenavfont">Website</span>
    </a>
</li>
```

Update the URL between `{}` with your parent site address.
Optional: Change the link name by updating the text between <span>...</span>.
* If you don't have a parent site, remove this entire block from the file.

## Change Carousel Images

You can replace the images shown in the carousel with your own.

Open the `/src/main/resources/static/style/style.css` file. Find a block like this:

```css 
.carousel_slide1 {
  background-image: url("/assets/carousel-1.png");
}
```

Change the image name `carousel-1.png` to your own image file name.
Save your image file in: `/src/main/resources/static/assets/`

### How It Works

The number at the end of the class name (e.g., carousel_slide1) determines the order of the image in the carousel.

- carousel_slide1 → 1st image
- carousel_slide2 → 2nd image

and so on...

The total number of .carousel_slideX blocks in style.css determines how many images the carousel will display.

## Remove or Replace the User Manual

You can remove the User Manual section entirely or replace the manuals with your own files.

Open `src/main/resources/templates/fragments/homeleftnavS.html`. Find this block of code:

```html
<li class="nav-item">
    <a href="/assets/User_Manual(2).pdf" download class="btn btn-primary" role="button" target="_blank">Download User Manual</a>
</li>
<br sec:authorize="hasAnyRole('ADMIN')">
<li class="nav-item" sec:authorize="hasAnyRole('ADMIN')">
    <a href="/assets/Admin_User_Manual(2).pdf" download class="btn btn-primary" role="button" target="_blank">Download Admin User Manual</a>
</li>
```

### To Remove the Manual Section

Delete the entire code block above from the file.

### To Replace with Your Own Manual

- For normal users:<br>
Replace `User_Manual.pdf` with your manual file name.

- For admin users:<br>
Replace `Admin_User_Manual.pdf` with your admin manual file name.

Save your manual files in: `src/main/resources/static/assets/`
