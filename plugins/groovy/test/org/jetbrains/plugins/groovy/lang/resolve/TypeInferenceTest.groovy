/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.groovy.lang.resolve

import com.intellij.psi.PsiIntersectionType
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiType
import org.intellij.lang.annotations.Language
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrListOrMap
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariableDeclaration
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrAssignmentExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.impl.GrClosureType
import org.jetbrains.plugins.groovy.util.TestUtils

import static com.intellij.psi.CommonClassNames.*

/**
 * @author ven
 */
public class TypeInferenceTest extends GroovyResolveTestCase {
  final String basePath = TestUtils.testDataPath + "resolve/inference/"

  @Override
  protected void setUp() {
    super.setUp()

    myFixture.addClass("package java.math; public class BigDecimal extends Number implements Comparable<BigDecimal> {}");
  }

  public void testTryFinallyFlow() {
    GrReferenceExpression ref = (GrReferenceExpression)configureByFile("tryFinallyFlow/A.groovy").element;
    final PsiType type = ref.type;
    assertTrue(type instanceof PsiIntersectionType);
    final PsiType[] conjuncts = ((PsiIntersectionType)type).conjuncts;
    assertEquals(conjuncts.length, 2);
  }

  public void testTryFinallyFlow1() {
    GrReferenceExpression ref = (GrReferenceExpression)configureByFile("tryFinallyFlow1/A.groovy").element;
    final PsiType type = ref.type;
    assertNotNull(type);
    assertTrue(type.equalsToText("java.lang.Integer"));
  }

  public void testTryFinallyFlow2() {
    GrReferenceExpression ref = (GrReferenceExpression)configureByFile("tryFinallyFlow2/A.groovy").element;
    final PsiType type = ref.type;
    assertNotNull(type);
    assertTrue(type.equalsToText("java.lang.Integer"));
  }

  public void testThrowVariable() {
    GrReferenceExpression ref = (GrReferenceExpression)configureByFile("throwVariable/A.groovy").element;
    final PsiType type = ref.type;
    assertNotNull(type);
    assertEquals("java.lang.Exception", type.canonicalText);
  }

  public void testGrvy852() {
    GrReferenceExpression ref = (GrReferenceExpression)configureByFile("grvy852/A.groovy").element;
    final PsiType type = ref.type;
    assertNotNull(type);
    assertEquals("java.lang.Object", type.canonicalText);
  }

  public void testGenericMethod() {
    GrReferenceExpression ref = (GrReferenceExpression)configureByFile("genericMethod/A.groovy").element;
    final PsiType type = ref.type;
    assertNotNull(type);
    assertEquals("java.util.List<java.lang.String>", type.canonicalText);
  }

  public void testCircular() {
    GrReferenceExpression ref = (GrReferenceExpression)configureByFile("circular/A.groovy").element;
    assertNull(ref.type);
  }

  public void testCircular1() {
    GrReferenceExpression ref = (GrReferenceExpression)configureByFile("circular1/A.groovy").element;
    assertNull(ref.type);
  }

  public void testClosure() {
    GrReferenceExpression ref = (GrReferenceExpression)configureByFile("closure/A.groovy").element;
    assertNotNull(ref.type);
  }

  public void testClosure1() {
    GrReferenceExpression ref = (GrReferenceExpression)configureByFile("closure1/A.groovy").element;
    assertTrue(ref.type.equalsToText("java.lang.Integer"));
  }

  public void testClosure2() {
    GrReferenceExpression ref = (GrReferenceExpression)configureByFile("closure2/A.groovy").element;
    assertTrue(ref.type.equalsToText("java.lang.Integer"));
  }

  public void testGrvy1209() {
    GrReferenceExpression ref = (GrReferenceExpression)configureByFile("grvy1209/A.groovy").element;
    assertTrue(ref.type.equalsToText("java.lang.String"));
  }

  public void testLeastUpperBoundClosureType() {
    GrReferenceExpression ref = (GrReferenceExpression)configureByFile("leastUpperBoundClosureType/A.groovy").element;
    assertInstanceOf(ref.type, GrClosureType.class);
  }

  public void testJavaLangClassType() {
    final GrReferenceExpression ref = (GrReferenceExpression)configureByFile("javaLangClassType/A.groovy").element;
    assertEquals("java.lang.String", ref.type.canonicalText);
  }

  public void testGenericWildcard() {
    final GrReferenceExpression ref = (GrReferenceExpression)configureByFile("genericWildcard/A.groovy").element;
    assertEquals("A<Base>", ref.type.canonicalText);
  }

  public void testArrayLikeAccessWithIntSequence() {
    final GrReferenceExpression ref = (GrReferenceExpression)configureByFile("arrayLikeAccessWithIntSequence/A.groovy").element;
    assertEquals("java.util.List<java.lang.Integer>", ref.type.canonicalText);
  }

  public void testArrayAccess() {
    final GrReferenceExpression ref = (GrReferenceExpression)configureByFile("arrayAccess/A.groovy");
    assertEquals(JAVA_LANG_STRING, ref.type.canonicalText);
  }

  public void testReturnTypeByTailExpression() {
    final GrReferenceExpression ref = (GrReferenceExpression)configureByFile("returnTypeByTailExpression/A.groovy");
    assertEquals(JAVA_LANG_STRING, ref.type.canonicalText);
  }

  public void testParameterWithBuiltinType() {
    GrReferenceExpression refExpr = (GrReferenceExpression)configureByFile("parameterWithBuiltinType/A.groovy");
    assertEquals("java.lang.Integer", refExpr.type.canonicalText);
  }

  public void testRawTypeInReturnExpression() {
    assertNotNull(resolve("A.groovy"));
  }

  public void testMethodCallInvokedOnArrayAccess() {
    final GrReferenceExpression reference = (GrReferenceExpression)configureByFile("A.groovy");
    assertNotNull(reference)
    assertNotNull(reference.type)
    assertEquals("java.lang.Integer", reference.type.canonicalText);
  }

  private void assertTypeEquals(String expected, String fileName) {
    final PsiReference ref = configureByFile(getTestName(true) + "/" + fileName);
    assertInstanceOf(ref, GrReferenceExpression.class);
    final PsiType type = ((GrReferenceExpression)ref).type;
    assertNotNull(type);
    assertEquals(expected, type.canonicalText);
  }

  public void testTypeOfGroupBy() {
    assertTypeEquals("java.util.Map<java.lang.Integer,java.util.List<java.lang.Integer>>", "A.groovy");
  }

  public void testConditionalExpressionWithNumericTypes() {
    assertTypeEquals("java.lang.Number", "A.groovy");
  }

  public void testImplicitCallMethod() {
    assertEquals("java.lang.String", ((GrExpression)configureByFile("A.groovy")).type.canonicalText);
  }

  public void testTupleWithNullInIt() {
    assertTypeEquals("java.util.ArrayList", "A.groovy");
  }

  public void testImplicitlyReturnedMethodCall() {
    assertTypeEquals("java.util.Map<BasicRange,java.util.Map<BasicRange,java.lang.Double>>", "A.groovy");
  }

  public void testInferWithClosureType() {
    assertTypeEquals("java.util.Date", "A.groovy");
  }

  public void testPlusEquals1() {
    assertTypeEquals("Test", "A.groovy");
  }

  public void testPlusEquals2() {
    assertTypeEquals("java.lang.String", "A.groovy");
  }

  public void testPlusEquals3() {
    assertTypeEquals("java.lang.String", "A.groovy");
  }

  public void testPlusEqualsClosure() {
    assertTypeEquals("java.lang.String", "A.groovy");
  }

  public void testGetAtClosure() {
    assertTypeEquals("java.lang.String", "A.groovy");
  }

  public void testPreferMethodOverloader() {
    assertTypeEquals("java.lang.String", "A.groovy");
  }

  public void testSafeInvocationInClassQualifier() {
    assertTypeEquals("java.lang.Class", "SafeInvocationInClassQualifier.groovy");
  }

  public void testReturnTypeFromMethodClosure() {
    assertTypeEquals("java.lang.String", "A.groovy");
  }

  public void testNoSOF() {
    final PsiReference ref = configureByFile(getTestName(true) + "/A.groovy");
    assertInstanceOf(ref, GrReferenceExpression.class);
    final PsiType type = ((GrReferenceExpression)ref).type;
    assertTrue(true); //test just should not fail with SOF exception
  }

  public void testTraditionalForVar() {
    assertTypeEquals(JAVA_LANG_INTEGER, "A.groovy");
  }

  public void testIncMethod() {
    assertTypeEquals(JAVA_LANG_INTEGER, "A.groovy");
  }

  public void testDGMFind() {
    assertTypeEquals("java.io.File", "a.groovy");
  }

  public void testMultiTypeParameter() {
    assertTypeEquals("X | Y", "a.groovy");
  }

  public void testTypeArgsInAccessor() {
    assertTypeEquals("Foo<java.lang.String>", "a.groovy");
  }

  public void testSingleParameterInStringInjection() {
    assertTypeEquals("java.io.StringWriter", "a.groovy");
  }

  void testIndexPropertyPlusAssigned() {
    GroovyFile file = myFixture.configureByText('a.groovy', '''
class X {
    def putAt(String s, X x){new Date()}

    def getAt(String s) {new X()}

    def plus(int i) {this}
}

map = new X()

map['i'] += 2
''') as GroovyFile
    GrAssignmentExpression assignment = file.topStatements[2] as GrAssignmentExpression
    assertType("X", assignment.type)
  }

  void testAllTypeParamsAreSubstituted() {
    assertTypeEquals('java.util.Map', 'a.groovy')
  }

  void testDiamond() {
    GroovyFile file = myFixture.configureByText('a.groovy', '''
List<String> list = new ArrayList<>()
List<Integer> l2

(list, l2) = [new ArrayList<>(), new ArrayList<>()]
''') as GroovyFile

    def statements = file.topStatements

    assertEquals('java.util.ArrayList<java.lang.String>', (statements[0] as GrVariableDeclaration).variables[0].initializerGroovy.type.canonicalText)
    assertEquals('java.util.ArrayList<java.lang.String>', ((statements[2] as GrAssignmentExpression).RValue as GrListOrMap).initializers[0].type.canonicalText)
    assertEquals('java.util.ArrayList<java.lang.Integer>', ((statements[2] as GrAssignmentExpression).RValue as GrListOrMap).initializers[1].type.canonicalText)
  }

  void testWildCardsNormalized() {
    assertTypeEquals(Object.canonicalName, 'a.groovy')
  }

  void testIndexPropertyInLHS() {
    assertTypeEquals("java.util.Map", 'a.groovy')
  }

  void testEmptyMapTypeArgs() {
    myFixture.configureByText('a.groovy', '''
class X<A, B> implements Map<A, B> {}

X<String, Integer> x = [:]
''')

    def type = ((myFixture.file as GroovyFile).statements[0] as GrVariableDeclaration).variables[0].initializerGroovy.type
    assertEquals("java.util.Map<java.lang.String,java.lang.Integer>", type.canonicalText)
  }

  void testRawCollectionsInCasts() {
    doTest('''\
String[] a = ["a"]
def b = a as ArrayList
def cc = b[0]
print c<caret>c''', String.canonicalName)
  }

  void testFind() {
    doTest('''\
def arr =  ['1', '2', '3'] as String[]
def found = arr.find({it=='1'})
print fou<caret>nd''', String.canonicalName)
  }

  void testFindAll() {
    doTest('''\
def arr =  ['1', '2', '3']
def found = arr.findAll({it==1})
print fou<caret>nd''', 'java.util.ArrayList<java.lang.String>')
  }

  void testFindAllForArray() {
    doTest('''\
def arr =  ['1', '2', '3'] as String[]
def found = arr.findAll({it==1})
print fou<caret>nd''', 'java.util.ArrayList<java.lang.String>')
  }

  void testFindAllForSet() {
    myFixture.addClass('''\
package java.util;
class HashSet<T> implements Set<T> {} ''')
    doTest('''\
def arr =  ['1', '2', '3'] as Set<String>
def found = arr.findAll({it==1})
print fou<caret>nd''', 'java.util.HashSet<java.lang.String>')
  }

  void testInferArgumentTypeFromMethod1() {
    doTest('''\
def bar(String s) {}

def foo(Integer a) {
  while(true) {
    bar(a)
    <caret>a.substring(2)
  }
}
''', '[java.lang.String,java.lang.Integer]')
  }

  void testInferArgumentTypeFromMethod2() {
    doTest('''\
def bar(String s) {}

def foo(Integer a) {
    bar(a)
    <caret>a.substring(2)
}
''', '[java.lang.String,java.lang.Integer]')
  }

  void testInferArgumentTypeFromMethod3() {
    doTest('''\
def bar(String s) {}

def foo(Integer a) {
    bar(a)
    print a
    <caret>a.substring(2)
}
''', '[java.lang.String,java.lang.Integer]')
  }

  void testInferArgumentTypeFromMethod4() {
    doTest('''\
def bar(String s) {}

def foo(Integer a) {
  while(true) {
    bar(a)
    print a
    <caret>a.substring(2)
  }
}
''', '[java.lang.String,java.lang.Integer]')
  }

  void testEmptyListOrListWithGenerics() {
    doTest('''\
def list = cond ? [1, 2, 3] : []
print lis<caret>t
''', "$JAVA_UTIL_LIST<$JAVA_LANG_INTEGER>")
  }

  void testEmptyListOrListWithGenerics2() {
    doTest('''\
def List<Integer> foo(){}
def list = cond ? foo() : []
print lis<caret>t
''', "$JAVA_UTIL_LIST<$JAVA_LANG_INTEGER>")
  }

  void testEmptyMapOrMapWithGenerics() {
    doTest('''\
def map = cond ? [1:'a', 2:'a', 3:'a'] : [:]
print ma<caret>p
''', "$JAVA_UTIL_MAP<$JAVA_LANG_STRING, $JAVA_LANG_STRING>")
  }

  void testEmptyMapOrMapWithGenerics2() {
    doTest('''\
def Map<String, String> foo(){}
def map = cond ? foo() : [:]
print ma<caret>p
''', "$JAVA_UTIL_MAP<$JAVA_LANG_STRING,$JAVA_LANG_STRING>")
  }

  void testSpread1() {
    myFixture.addClass('''\
class A {
  String getString() {return "a";}
}''')
    doTest('''\
[new A()].stri<caret>ng
''', "$JAVA_UTIL_ARRAY_LIST<$JAVA_LANG_STRING>")
  }

  void testSpread2() {

    myFixture.addClass('''\
class A {
  String getString() {return "a";}
}''')
    doTest('''\
class Cat {
  static getFoo(String b) {2}
}
use(Cat) {
  [new A()].string.fo<caret>o
}
''', "$JAVA_UTIL_ARRAY_LIST<$JAVA_LANG_INTEGER>")
  }

  void testSpread3() {
    myFixture.addClass('''\
class A {
  String getString() {return "a";}
}''')
    doTest('''\
[[new A()]].stri<caret>ng
''', "$JAVA_UTIL_ARRAY_LIST<$JAVA_UTIL_ARRAY_LIST<$JAVA_LANG_STRING>>")
  }

  void testSpread4() {
    myFixture.addClass('''\
class A {
  String getString() {return "a";}
}''')
    doTest('''\
class Cat {
  static getFoo(String b) {2}
}

use(Cat){
  [[new A()]].string.fo<caret>o
}
''', "$JAVA_UTIL_ARRAY_LIST<$JAVA_UTIL_ARRAY_LIST<$JAVA_LANG_INTEGER>>")
  }

  void testInstanceOfInferring1() {
    doTest('''\
def bar(oo) {
  boolean b = oo instanceof String || oo != null
  o<caret>o
}
''', null)
  }

  void testInstanceOfInferring2() {
    doTest('''\
def bar(oo) {
  boolean b = oo instanceof String || o<caret>o != null
  oo
}
''', null)
  }

  void testInstanceOfInferring3() {
    doTest('''\
def bar(oo) {
  boolean b = oo instanceof String && o<caret>o != null
  oo
}
''', String.canonicalName)
  }

  void testInstanceOfInferring4() {
    doTest('''\
def bar(oo) {
  boolean b = oo instanceof String && oo != null
  o<caret>o
}
''', null)
  }

  void testInstanceOfInferring5() {
    doTest('''\
def foo(def oo) {
  if (oo instanceof String && oo instanceof CharSequence) {
    oo
  }
  else {
    o<caret>o
  }

}
''', null)
  }

  void testInstanceOfInferring6() {
    doTest('''\
def foo(bar) {
  if (!(bar instanceof String) && bar instanceof Runnable) {
    ba<caret>r
  }
}''', 'java.lang.Runnable')
  }

  void testInString() {
    doTest '''\
def foo(ii) {
  if (ii in String)
    print i<caret>i
}''', 'java.lang.String'
  }

  void testIndexProperty() {
    doTest('''\
private void getCommonAncestor() {
    def c1 = [new File('a')]
    for (int i = 0; i < 2; i++) {
        if (c1[i] != null) break
        def cur = c1[i]
        print cu<caret>r
    }
}
''', 'java.io.File')

  }

  void testWildcardClosureParam() {
    doTest('''\
class Tx {
    def methodOfT() {}
}

def method(List<? extends Tx> t) {
    t.collect { print i<caret>t }
}
''', 'Tx')
  }

  void testAssert() {
    doTest('''\
def foo(def var) {
  assert var instanceof String
  va<caret>r.isEmpty()
}
''', 'java.lang.String')
  }

  void testUnresolvedSpread() {
    doTest('''\
def xxx = abc*.name
print xx<caret>x''', 'java.util.List')
  }

  void testThisInCategoryClass() {
    doTest('''\
class Cat {}

@groovy.lang.Category(Cat)
class Any {
  void foo() {
    print th<caret>is
  }
}
''', 'Cat')
  }

  void testNormalizeTypeFromMap() {
    doTest('''\
        def pp = new HashMap<?, ?>().a
        print p<caret>p
''', 'java.lang.Object')
  }

  void testUnary() {
    doExprTest('~/abc/', 'java.util.regex.Pattern')
  }

  void testUnary2() {
    doExprTest('-/abc/', null)
  }

  void testUnary3() {
    doExprTest('''
      class A {
        def bitwiseNegate() {'abc'}
      }
      ~new A()
''', 'java.lang.String')
  }

  void testPlus1() {
    doExprTest('2+2', 'java.lang.Integer')
  }

  void testPlus2() {
    doExprTest('2f+2', 'java.lang.Double')
  }

  void testPlus3() {
    doExprTest('2f+2f', 'java.lang.Double')
  }

  void testPlus4() {
    doExprTest('2.5+2', 'java.math.BigDecimal')
  }

  void testMultiply1() {
    doExprTest('2*2', 'java.lang.Integer')
  }

  void testMultiply2() {
    doExprTest('2f*2f', 'java.lang.Double')
  }

  void testMultiply3() {
    doExprTest('2d*2d', 'java.lang.Double')
  }

  void testMultiply4() {
    doExprTest('2.4*2', 'java.math.BigDecimal')
  }

  void testMultiply5() {
    doExprTest('((byte)2)*((byte)2)', 'java.lang.Integer')
  }

  void testMultiply6() {
    doExprTest('"abc"*"cde"', 'java.lang.String') //expected number as a right operand
  }

  void testMultiply7() {
    doExprTest('''
      class A {
        def multiply(A a) {new B()}
      }
      class B{}
      new A()*new A()
''', 'B')
  }

  void testMultiply8() {
    doExprTest('''
      class A { }
      new A()*new A()
''', null)
  }

  void testDiv1() {
    doExprTest('1/2', 'java.math.BigDecimal')
  }

  void testDiv2() {
    doExprTest('1/2.4', 'java.math.BigDecimal')
  }

  void testDiv3() {
    doExprTest('1d/2', 'java.lang.Double')
  }

  void testDiv4() {
    doExprTest('1f/2', 'java.lang.Double')
  }

  void testDiv5() {
    doExprTest('1f/2.4', 'java.lang.Double')
  }

  private void doTest(@Language("Groovy") String text, String type) {
    def file = myFixture.configureByText('_.groovy', text)
    def ref = file.findReferenceAt(myFixture.editor.caretModel.offset) as GrReferenceExpression
    def actual = ref.type
    assertType(type, actual)
  }

  private void doExprTest(@Language("Groovy") String text, String expectedType) {
    GroovyFile file = myFixture.configureByText('_.groovy', text) as GroovyFile
    GrStatement lastStatement = file.statements.last()
    assertInstanceOf lastStatement, GrExpression
    assertType(expectedType, (lastStatement as GrExpression).type)
  }
}
