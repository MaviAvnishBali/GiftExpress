package com.giftexpress.app.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R
import com.giftexpress.app.ui.theme.Gilroy

@Composable
fun StaticContentScreen(
    title: String,
    onBackClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Text(
                    text = title,
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    content()
                }
            }
        }
    }
}

@Composable
fun AboutUsScreen(onBackClick: () -> Unit) {
        StaticContentScreen(title = "About Gift Express", onBackClick = onBackClick) {
            Paragraph("Giftexpress.com is an online retailer of genuine brand-name fragrances headquartered in New Jersey. Established in 2016, the company has consistently provided high-quality products at competitive prices accompanied by exceptional customer service. As a family-owned and operated business, Giftexpress.com is committed to facilitating a seamless shopping experience for its customers.")
            Paragraph("The company ranks among the largest online discount fragrance retailers, specializing in authentic brand-name fragrances for both men and women.")
            Paragraph("With an extensive inventory of over 5,000 genuine brand-name fragrances, Giftexpress.com ensures that all products are authentic and of high quality.")
            Paragraph("The company takes pride in its integrity, as it does not sell counterfeit or imitation products. Should any customer be dissatisfied with their order for any reason, they may return it for a full refund.")
        }
    }

@Composable
fun ShippingInfoScreen(onBackClick: () -> Unit) {
    StaticContentScreen(title = "Shipping Information", onBackClick = onBackClick) {
        Paragraph("Any orders placed on the weekend will ship the following business day.")
        SectionHeader("FREE U.S. Shipping on orders with product total of $59.95 or more (excluding shipping and other charges). Otherwise, shipping is $7.95.")
        Paragraph("Orders shipped through Ground Shipping take 5-7 business days (Excluding Weekends and Holidays) to arrive. Orders are shipped via USPS, FedEx and UPS.")
        SectionHeader("Express Shipping Rates Are Below:")
        BulletItem("2nd Day Delivery (Excluding Weekends and Holidays) > $24.95")
        BulletItem("Next Day Delivery (Excluding Weekends and Holidays) > $39.95")
        BulletItem("3 Days Delivery (Excluding Weekends and Holidays) > $14.95 (Up to 8 quantity)")
        SectionHeader("Please note we do not provide Express Shipping to APO, Hawaii, and Puerto Rico.")
        Paragraph("For Puerto Rico, Alaska, and Hawaii, delivery may take 7 to 14 business days.")
        Paragraph("Orders are shipped through Ground Shipping and it takes 5-7 business days for final delivery. (Excluding Weekends and Holidays).")
        SectionHeader("Please note: There will be a one-day handling time for all orders shipped via Ground Shipping.")
        Paragraph("GiftExpress.com will ship all orders based on the customer's requirements but we cannot guarantee a carrier will deliver on a specified date since they are a third-party service.")
    }
}

@Composable
fun PrivacyPolicyScreen(onBackClick: () -> Unit) {
    StaticContentScreen(title = "Privacy Policy", onBackClick = onBackClick) {
        Paragraph("Thank you for visiting our web site. This privacy policy tells you how we use personal information collected at this site. Please read this privacy policy before using the site or submitting any personal information. By using the site, you are accepting the conditions described in this privacy policy. These conditions may be changed at any time. You are encouraged to review the privacy policy whenever you visit the site to make sure that you understand how any personal information you provide will be used.")
        Paragraph("Note, the privacy practices set forth in this privacy policy are for this web site only. If you link to other web sites, please review the privacy policies posted at those sites.")
        SectionHeader("Collection Of Information")
        Paragraph("We collect personally identifiable information, like names, postal addresses, email addresses, etc., when voluntarily submitted by our visitors. The information you provide is used to fulfill your specific request. This information is only used to fulfill your specific request, unless you give us permission to use it in another manner, for example to add you to one of our mailing lists.")
        SectionHeader("Cookie/tracking Technology")
        Paragraph("The Site may use cookie and tracking technology depending on the features offered. Cookie and tracking technology are useful for gathering information such as browser type and operating system, tracking the number of visitors to the Site, and understanding how visitors use the Site. Cookies can also help customize the Site for visitors. Personal information cannot be collected via cookies and other tracking technology, however, if you previously provided personally identifiable information, cookies may be tied to such information. Aggregate cookie and tracking information may be shared with third parties.")
        SectionHeader("Distribution Of Information")
        Paragraph("Giftexpress.com can any time modify the User Agreement without any prior notification to its customers. We may share information with governmental agencies or other companies assisting us in fraud prevention or investigation. We may do so when:")
        BulletItem("permitted or required by law; or,")
        BulletItem("trying to protect against or prevent actual or potential fraud or unauthorized transactions; or,")
        BulletItem("investigating fraud which has already taken place. The information is not provided to these companies for marketing purposes.")
        SectionHeader("Commitment To Data Security")
        Paragraph("Your personally identifiable information is kept secure. Only authorized employees, agents and contractors (who have agreed to keep information secure and confidential) have access to this information. All emails and newsletters from this site allow you to opt out of further mailings.")
        SectionHeader("Privacy Contact Information")
        Paragraph("If you have any questions, concerns, or comments about our privacy policy you may contact us using the information below: By E-Mail: csr@giftexpress.com Toll Free: 1.844.800. 6161")
        Paragraph("We reserve the right to make changes to this policy. Any changes to this policy will be posted.")
        SectionHeader("Your Access To And Control Over Information")
        Paragraph("You may opt out of any future contacts from us at any time. You can do the following at any time by contacting us via the email address or phone number given on our website:")
        BulletItem("See what data we have about you, if any.")
        BulletItem("Change/correct any data we have about you.")
        BulletItem("Have us delete any data we have about you.")
        BulletItem("Express any concern you have about our use of your data.")
    }
}

@Composable
fun TermsConditionsScreen(onBackClick: () -> Unit) {
    StaticContentScreen(title = "Terms and Conditions", onBackClick = onBackClick) {
        Paragraph("The following document is in your benefit, while you are accessing the site i.e. www.giftexpress.com and its services provided to you. Please keep in mind certain terms & conditions.")
        SectionHeader("Membership")
        Paragraph("Only members can use the site by signing in and assured of approving the terms & conditions of the company.")
        BulletItem("Giftexpress.com reserves the rights to end your membership and refuse to provide you with the authority to use the Site, if we find any unfavorable act against the company.")
        BulletItem("In future the site will not be available to a person whose membership has been terminated by Giftexpress.com for any reason.")
        BulletItem("SSL Layer encrypts all information you input including your credit card number, before it is sent to us")
        BulletItem("For opting membership your age should be 18 years or over and if you are a minor i.e. under the age of 18 years but at least 13 years of age you may use this Site only under the supervision of your parents or legal guardian who agrees with the Terms & conditions.")
        SectionHeader("Refund Policy")
        Paragraph("Customer satisfaction has been a #1 priority for us. We stand behind every product we offer. We'll gladly give you a Full Refund on returned merchandise, the product(s) must be returned within 14 days of the delivery date.")
        Paragraph("The product(s) must be returned in the exact condition received and in its original packaging.")
        Paragraph("Any shipping cost paid by you to return the product to us will not be refunded.")
        Paragraph("Shipping cost is non-refundable for undelivered, unclaimed, returned and refused packages, unless an error was made by us.")
        SectionHeader("Returns Should be sent to:")
        Paragraph("GiftExpress.com Returns\n3775 Park Ave\nUnit 5A\nEdison NJ 08820")
        SectionHeader("Modification Of Terms And Conditions Of Service")
        Paragraph("Giftexpress.com can any time modify the User Agreement without any prior notification to its customers.")
        BulletItem("We will inform you about the changes in the User Agreement via e-mail that is provided by you while registering on giftexpress.com")
        BulletItem("You should regularly review the User Agreement on giftexpress.com in case modified User Agreement is not satisfactory to you, you should discontinue using the services.")
        BulletItem("However, if you continue to use the services, you shall be considered to have agreed with the terms & conditions and abide by the modified User Agreement.")
        SectionHeader("Copyright")
        Paragraph("All content included on this site, such as text, graphics, logos, button icons, images, audio clips, digital downloads, data compilations, and software, is the property of this site's owner. The compilation of all content on this site is the exclusive property of this site's owner. All software used on this site is the property of this site's owner or its software suppliers.")
        SectionHeader("Disclaimer Of Warranties And Limitation Of Liability")
        Paragraph("This Site Is Provided On An \"As Is\" And \"As Available\" Basis. No Representations Or Warranties Of Any Kind Are Made, Express Or Implied, As To The Operation Of This Site Or The Information, Content, Materials, Or Products Included On This Site. You Expressly Agree That Your Use Of This Site Is At Your Sole Risk.")
        Paragraph("To The Full Extent Permissible By Applicable Law, This Site's Owner Disclaims All Warranties, Express Or Implied, Including, But Not Limited To, Implied Warranties Of Merchantability And Fitness For A Particular Purpose. This Site's Owner Does Not Warrant That This Site, Its Servers, Or E-mail Sent From This Site Are Free Of Viruses Or Other Harmful Components. This Site's Owner Will Not Be Liable For Any Damages Of Any Kind Arising From The Use Of This Site, Including, But Not Limited To Direct, Indirect, Incidental, Punitive, And Consequential Damages.")
        Paragraph("Certain State Laws Do Not Allow Limitations On Implied Warranties Or The Exclusion Or Limitation Of Certain Damages. If These Laws Apply To You, Some Or All Of The Above Disclaimers, Exclusions, Or Limitations May Not Apply To You, And You Might Have Additional Rights.")
    }
}

@Composable
fun BulletItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_check_circle),
            contentDescription = null,
            tint = Color(0xFFC62828), // Reddish color from image
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontFamily = Gilroy,
            fontSize = 14.sp,
            color = Color.DarkGray,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        fontFamily = Gilroy,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color.Black,
        modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
    )
}

@Composable
fun Paragraph(text: String) {
    Text(
        text = text,
        fontFamily = Gilroy,
        fontSize = 14.sp,
        color = Color.DarkGray,
        lineHeight = 20.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
