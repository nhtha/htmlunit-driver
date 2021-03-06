// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.internal.FindsByClassName;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.internal.FindsByLinkText;
import org.openqa.selenium.internal.FindsByName;
import org.openqa.selenium.internal.FindsByTagName;
import org.openqa.selenium.internal.FindsByXPath;

@RunWith(JUnit4.class)
public class ByTest {

  @Test
  public void shouldUseFindsByNameToLocateElementsByName() {
    final AllDriver driver = mock(AllDriver.class);

    By.name("cheese").findElement(driver);
    By.name("peas").findElements(driver);

    verify(driver).findElementByName("cheese");
    verify(driver).findElementsByName("peas");
    verifyNoMoreInteractions(driver);
  }

  @Test
  public void shouldUseXPathToFindByNameIfDriverDoesNotImplementFindsByName() {
    final OnlyXPath driver = mock(OnlyXPath.class);

    By.name("cheese").findElement(driver);
    By.name("peas").findElements(driver);

    verify(driver).findElementByXPath(".//*[@name = 'cheese']");
    verify(driver).findElementsByXPath(".//*[@name = 'peas']");
    verifyNoMoreInteractions(driver);
  }

  @Test
  public void fallsBackOnXPathIfContextDoesNotImplementFallsById() {
    OnlyXPath driver = mock(OnlyXPath.class);

    By.id("foo").findElement(driver);
    By.id("bar").findElements(driver);

    verify(driver).findElementByXPath(".//*[@id = 'foo']");
    verify(driver).findElementsByXPath(".//*[@id = 'bar']");
    verifyNoMoreInteractions(driver);
  }

  @Test
  public void doesNotUseXPathIfContextFindsById() {
    AllDriver context = mock(AllDriver.class);

    By.id("foo").findElement(context);
    By.id("bar").findElements(context);

    verify(context).findElementById("foo");
    verify(context).findElementsById("bar");
    verifyNoMoreInteractions(context);
  }

  @Test
  public void searchesByTagNameIfSupported() {
    AllDriver context = mock(AllDriver.class);

    By.tagName("foo").findElement(context);
    By.tagName("bar").findElements(context);

    verify(context).findElementByTagName("foo");
    verify(context).findElementsByTagName("bar");
    verifyNoMoreInteractions(context);
  }

  @Test
  public void searchesByXPathIfCannotFindByTagName() {
    OnlyXPath context = mock(OnlyXPath.class);

    By.tagName("foo").findElement(context);
    By.tagName("bar").findElements(context);

    verify(context).findElementByXPath(".//foo");
    verify(context).findElementsByXPath(".//bar");
    verifyNoMoreInteractions(context);
  }

  @Test
  public void searchesByClassNameIfSupported() {
    AllDriver context = mock(AllDriver.class);

    By.className("foo").findElement(context);
    By.className("bar").findElements(context);

    verify(context).findElementByClassName("foo");
    verify(context).findElementsByClassName("bar");
    verifyNoMoreInteractions(context);
  }

  @Test
  public void searchesByXPathIfFindingByClassNameNotSupported() {
    OnlyXPath context = mock(OnlyXPath.class);

    By.className("foo").findElement(context);
    By.className("bar").findElements(context);

    verify(context).findElementByXPath(
        ".//*[contains(concat(' ',normalize-space(@class),' '),' foo ')]");
    verify(context).findElementsByXPath(
        ".//*[contains(concat(' ',normalize-space(@class),' '),' bar ')]");
    verifyNoMoreInteractions(context);
  }

  @Test
  public void innerClassesArePublicSoThatTheyCanBeReusedElsewhere() {
    assertThat(new By.ByXPath("a").toString(), equalTo("By.xpath: a"));
    assertThat(new By.ById("a").toString(), equalTo("By.id: a"));
    assertThat(new By.ByClassName("a").toString(), equalTo("By.className: a"));
    assertThat(new By.ByLinkText("a").toString(), equalTo("By.linkText: a"));
    assertThat(new By.ByName("a").toString(), equalTo("By.name: a"));
    assertThat(new By.ByTagName("a").toString(), equalTo("By.tagName: a"));
    assertThat(new By.ByCssSelector("a").toString(), equalTo("By.cssSelector: a"));
    assertThat(new By.ByPartialLinkText("a").toString(), equalTo("By.partialLinkText: a"));
  }

  // See http://code.google.com/p/selenium/issues/detail?id=2917
  @Test
  public void testHashCodeDoesNotFallIntoEndlessRecursion() {
    By locator = new By() {
      @Override
      public List<WebElement> findElements(SearchContext context) {
        return null;
      }
    };
    locator.hashCode();
  }

  private interface AllDriver
      extends FindsById, FindsByLinkText, FindsByName, FindsByXPath, FindsByTagName,
              FindsByClassName, SearchContext {
    // Place holder
  }

  private interface OnlyXPath extends FindsByXPath, SearchContext {

  }
}
