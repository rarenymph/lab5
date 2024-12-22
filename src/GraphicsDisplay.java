import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.JPanel;
import java.util.Stack;
import static java.lang.Math.abs;

            @SuppressWarnings("serial")
            public class GraphicsDisplay extends JPanel {
                // Список координат точек для построения графика
                private Double[][] graphicsData;
                private List<Double[]> graphicsDataList = new ArrayList<>();
                // Флаговые переменные, задающие правила отображения графика
                private boolean showAxis = true;
                private boolean showMarkers = true;
                private boolean showPolygons = false;
                // Флаг для включения режима поворота
                private boolean rotateGraph = false;
                private boolean changeSelectedPoint = false;
                private boolean graphicsIsOpen = false;

                private Point2D.Double selectedPoint;
                private Point2D.Double zoomPoint1;
                private Point2D.Double zoomPoint2;
                private Point2D.Double scalePoint1;
                private Point2D.Double scalePoint2;
                private Point2D.Double movedPoint;

                // Границы диапазона пространства, подлежащего отображению
                private double minX;
                private double maxX;
                private double minY;
                private double maxY;
                // Используемый масштаб отображения
                private double scale;
                // Различные стили черчения линий
                private BasicStroke graphicsStroke;
                private BasicStroke zoomWindowStroke;
                private BasicStroke axisStroke;
                private BasicStroke markerStroke;
                // Различные шрифты отображения надписей
                private Font axisFont;
                private List<Double> oXpoints;
                private List<Double> oYpoints;

                private Stack<ArrayList<Double[]>> graphicMemoryStack = new Stack<>();







                public GraphicsDisplay() {
// Цвет заднего фона области отображения - белый
                    setBackground(Color.WHITE);

// Сконструировать необходимые объекты, используемые в рисовании
// Перо для рисования графика
                    float[] dashPattern = {17,10,1,10,1,10,5,10,5,10};
                    graphicsStroke = new BasicStroke(
                            5,                      // Толщина линии
                            BasicStroke.CAP_SQUARE,  // Квадратные концы
                            BasicStroke.JOIN_MITER,  // Прямые углы в точках соединения
                            22f,                     // Митра соединений
                            dashPattern,             // Шаблон пунктиров
                            0f                       // Начальная фаза (сдвиг)
                    );
                    float[] dashPattern2 = {10};
                    zoomWindowStroke = new BasicStroke(
                            2,                      // Толщина линии
                            BasicStroke.CAP_SQUARE,  // Квадратные концы
                            BasicStroke.JOIN_MITER,  // Прямые углы в точках соединения
                            22f,                     // Митра соединений
                            dashPattern2,             // Шаблон пунктиров
                            0f                       // Начальная фаза (сдвиг)
                    );

// Перо для рисования осей координат
                    axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Перо для рисования контуров маркеров
                    markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Шрифт для подписей осей координат
                    axisFont = new Font("Serif", Font.BOLD, 36);

                    addMouseListener(new MouseListener() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getButton() == MouseEvent.BUTTON3 && !graphicMemoryStack.isEmpty()) {
                                // Возвращаемся к предыдущему увеличению
                                ArrayList<Double[]> temp = graphicMemoryStack.pop();
                                graphicsData = temp.toArray(graphicsData);
                                repaint(); // Перерисовываем график с предыдущими границами
                            }
                        }

                        @Override
                        public void mousePressed(MouseEvent e) {
                            int button = e.getButton();


                            if (button == MouseEvent.BUTTON1 && selectedPoint == null && graphicsIsOpen) {
                                zoomPoint1 = new Point2D.Double(e.getX(), e.getY());
                                scalePoint1 = zoomPoint1;
                                repaint();
                            }
                            if (button == MouseEvent.BUTTON1 && selectedPoint != null && graphicsIsOpen) {
                                changeSelectedPoint = true;
                                movedPoint = new Point2D.Double(e.getX(),e.getY());
                                repaint();
                            }

                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                            if(graphicsIsOpen) {
                                scalePoint2 = zoomPoint2;
                                zoomPoint2 = null;
                                changeSelectedPoint = false;
                                movedPoint = null;
                                repaint();
                            }
                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {

                        }

                        @Override
                        public void mouseExited(MouseEvent e) {

                        }
                    });
                    addMouseMotionListener(new MouseMotionListener() {
                        @Override
                        public void mouseDragged(MouseEvent e) {
                            if(graphicsIsOpen) {
                                if (zoomPoint1 != null) {
                                    zoomPoint2 = new Point2D.Double(e.getX(), e.getY());
                                    repaint();
                                }
                                if (changeSelectedPoint) {
                                    movedPoint.y = (new Point2D.Double(e.getX(), e.getY())).y;
                                    repaint();
                                }
                                for (Double[] point : graphicsData) {
                                    Point2D.Double center = xyToPoint(point[0], point[1]);
                                    if ((center.getX() - 5) < e.getX() && e.getX() < (center.getX() + 5) && (center.getY() - 5) < e.getY() && e.getY() < (center.getY() + 5)) {

                                        selectedPoint = center;
                                        repaint();
                                        break;
                                    } else {
                                        selectedPoint = null;
                                        repaint();
                                    }
                                }

                            }
                        }

                        @Override
                        public void mouseMoved(MouseEvent e) {
                            if(graphicsIsOpen)
                                for(Double[] point : graphicsData)
                                {
                                    Point2D.Double center = xyToPoint(point[0], point[1]);
                                    if((center.getX()-5) < e.getX() && e.getX() < (center.getX()+5) && (center.getY()-5) < e.getY() && e.getY() < (center.getY()+5))
                                    {

                                        selectedPoint = center;
                                        repaint();
                                        break;
                                    }
                                    else {
                                        selectedPoint = null;
                                        repaint();
                                    }
                                }


                        }
                    });
                }

                // Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
                // главного окна приложения в случае успешной загрузки данных
                public void showGraphics(Double[][] graphicsData) {
// Сохранить массив точек во внутреннем поле класса
                    this.graphicsData = graphicsData;
                    graphicsIsOpen = true;
// Запросить перерисовку компонента, т.е. неявно вызвать paintComponent()
                    repaint();
                }

                // Методы-модификаторы для изменения параметров отображения графика
// Изменение любого параметра приводит к перерисовке области
                public void setShowAxis(boolean showAxis) {
                    this.showAxis = showAxis;
                    repaint();
                }

                public void setShowMarkers(boolean showMarkers) {
                    this.showMarkers = showMarkers;
                    repaint();
                }
                public void setShowPolygons(boolean showPolygons) {
                    this.showPolygons = showPolygons;
                    repaint();
                }

                public void setRotateGraph(boolean rotateGraph) {
                    this.rotateGraph = rotateGraph;
                    repaint();
                }

                public void saveGraphicToFile(File selectedFile)
                {
                    try {
// Создать новый символьный поток вывода, направленный в указанный файл
                        PrintStream out = new PrintStream(selectedFile);
// Записать в поток вывода заголовочные сведения



// Записать в поток вывода значения в точках
                        for (int i = 0; i<graphicsData.length; i++) {
                            out.println(graphicsData[i][0]
                                    + " " + graphicsData[i][1]);
                        }
// Закрыть поток
                        out.close();
                    } catch (FileNotFoundException e) {
// Исключительную ситуацию "ФайлНеНайден" можно не
// обрабатывать, так как мы файл создаѐм, а не открываем
                    }
                }



                public void searchPolygon(Graphics g) {
                    oXpoints = new ArrayList<>();
                    oYpoints = new ArrayList<>();
                    List<Double[]> allPoints = new ArrayList<>();

                    // Проход по всем точкам графика и добавление точек пересечения сразу после их нахождения
                    for (int i = 0; i < graphicsData.length - 1; i++) {
                        Double[] point1 = graphicsData[i];
                        Double[] point2 = graphicsData[i + 1];

                        // Добавляем первую точку в общий список
                        allPoints.add(point1);

                        // Проверка на совпадение с осью X
                        if (point1[1] == 0) {
                            oXpoints.add(point1[0]);
                            oYpoints.add(point1[1]);
                        }

                        // Поиск точки пересечения при изменении знака y-координат
                        if (point1[1] * point2[1] < 0) {
                            double xIntersect = point1[0] - (point2[0] - point1[0]) * point1[1] / (point2[1] - point1[1]);
                            oXpoints.add(xIntersect);
                            oYpoints.add(0.0);
                            allPoints.add(new Double[]{xIntersect, 0.0}); // Вставка точки пересечения сразу после точки point1
                        }
                    }

                    // Добавляем последнюю точку графика
                    allPoints.add(graphicsData[graphicsData.length - 1]);

                    // Теперь у нас есть все точки в правильном порядке, и мы можем строить области
                    List<Double> areas = new ArrayList<>();
                    List<Point2D.Double> areaCenters = new ArrayList<>();

                    GeneralPath polygon = null;
                    double area = 0;
                    boolean isInsideArea = false;
                    double xCenter = 0;
                    double yCenter = 0;
                    int pointCount = 0;

                    Graphics2D canvas = (Graphics2D) g;
                    canvas.setColor(Color.BLACK);

                    // Построение областей
                    for (int i = 0; i < allPoints.size() - 1; i++) {
                        Double[] point1 = allPoints.get(i);
                        Double[] point2 = allPoints.get(i + 1);

                        // Начинаем новую область
                        if (point1[1] == 0 && !isInsideArea) {
                            polygon = new GeneralPath();
                            polygon.moveTo(xyToPoint(point1[0], point1[1]).getX(), xyToPoint(point1[0], point1[1]).getY());
                            isInsideArea = true;
                            xCenter = 0;
                            yCenter = 0;
                            pointCount = 0;
                            area = 0;
                        }

                        // Если мы внутри области, строим многоугольник
                        if (isInsideArea) {
                            polygon.lineTo(xyToPoint(point2[0], point2[1]).getX(), xyToPoint(point2[0], point2[1]).getY());

                            // Накапливаем данные для вычисления центра области
                            xCenter += point1[0];
                            yCenter += point1[1];
                            pointCount++;

                            // Вычисляем площадь методом трапеций
                            area += (point2[0] - point1[0]) * (point1[1] + point2[1]) / 2;

                            // Закрываем область, когда встречаем y=0 снова
                            if (point2[1] == 0) {
                                xCenter /= pointCount;
                                yCenter /= pointCount;

                                areas.add(Math.abs(area));
                                areaCenters.add(new Point2D.Double(xCenter, yCenter));

                                // Закраска области
                                polygon.closePath();
                                canvas.fill(polygon);

                                // Сброс параметров для следующей области
                                area = 0;
                                isInsideArea = false;
                            }
                        }
                    }

                    // Отображение значений площади на графике
                    canvas.setFont(new Font("Arial", Font.PLAIN, 12));
                    canvas.setColor(Color.BLACK);
                    for (int i = 0; i < areas.size(); i++) {
                        Point2D.Double center = areaCenters.get(i);
                        String areaText = String.format("%.2f", areas.get(i));

                        if (areas.get(i) != 0)
                            canvas.drawString(areaText, (float) xyToPoint(center.x, 0).getX(), (float) xyToPoint(0, center.y).getY());
                    }
                }

                public void zoom()
                {
                    if( scalePoint1 != null && scalePoint2 != null)
                    {
                        ArrayList<Double[]> tempList = new ArrayList<>();
                        for(Double[] point : graphicsData)
                        {
                            Point2D.Double temp = xyToPoint(point[0],point[1]);
                            tempList.add(new Double[]{point[0],point[1]});

                            if(scalePoint1.getX() <= temp.getX() && scalePoint2.getX() >= temp.getX() && scalePoint1.getY() <= temp.getY() && scalePoint2.getY() >= temp.getY())
                            {

                                graphicsDataList.add(new Double[]{point[0],point[1]});
                            }
                        }

                        graphicMemoryStack.add(tempList);
                        graphicsData = new Double[graphicsDataList.size()][1];
                        graphicsData =graphicsDataList.toArray(graphicsData);

                        // Вывод значений в консоль
                        for (Double[] point : graphicsData) {
                            System.out.println("X: " + point[0] + ", Y: " + point[1]);
                        }
                        repaint();
                    }
                }


                // Метод отображения всего компонента, содержащего график
                public void paintComponent(Graphics g) {
                    /* Шаг 1 - Вызвать метод предка для заливки области цветом заднего фона
                     * Эта функциональность - единственное, что осталось в наследство от
                     * paintComponent класса JPanel
                     */

                    super.paintComponent(g);
// Шаг 2 - Если данные графика не загружены (при показе компонента при запуске программы) - ничего не делать
                    if (graphicsData==null || graphicsData.length==0) return;
// Шаг 3 - Определить минимальное и максимальное значения для координат X и Y
// Это необходимо для определения области пространства, подлежащей отображению
// Еѐ верхний левый угол это (minX, maxY) - правый нижний это (maxX, minY)



                    minX = graphicsData[0][0];
                    maxX = graphicsData[graphicsData.length-1][0];
                    minY = graphicsData[0][1];
                    maxY = minY;


// Найти минимальное и максимальное значение функции
                    for (int i = 1; i<graphicsData.length; i++) {
                        if (graphicsData[i][1]<minY) {
                            minY = graphicsData[i][1];
                        }
                        if (graphicsData[i][1]>maxY) {
                            maxY = graphicsData[i][1];
                        }
                    }

/* Шаг 4 - Определить (исходя из размеров окна) масштабы по осям X
и Y - сколько пикселов
* приходится на единицу длины по X и по Y
*/
                    double scaleX = getSize().getWidth() / (maxX - minX);
                    double scaleY = getSize().getHeight() / (maxY - minY);
// Шаг 5 - Чтобы изображение было неискажѐнным - масштаб должен быть одинаков
// Выбираем за основу минимальный
                    scale = Math.min(scaleX, scaleY);
// Шаг 6 - корректировка границ отображаемой области согласно выбранному масштабу
                    if (scale==scaleX) {
/* Если за основу был взят масштаб по оси X, значит по оси Y
делений меньше,
* т.е. подлежащий визуализации диапазон по Y будет меньше
высоты окна.
* Значит необходимо добавить делений, сделаем это так:
* 1) Вычислим, сколько делений влезет по Y при выбранном
масштабе - getSize().getHeight()/scale
* 2) Вычтем из этого сколько делений требовалось изначально
* 3) Набросим по половине недостающего расстояния на maxY и
minY
*/

                        double yIncrement = (getSize().getHeight()/scale - (maxY -
                                minY))/2;
                        maxY += yIncrement;
                        minY -= yIncrement;
                    }
                    if (scale==scaleY) {
// Если за основу был взят масштаб по оси Y, действовать по аналогии
                        double xIncrement = (getSize().getWidth()/scale - (maxX -
                                minX))/2;
                        maxX += xIncrement;
                        minX -= xIncrement;
                    }
// Шаг 7 - Сохранить текущие настройки холста
                    Graphics2D canvas = (Graphics2D) g;

                    // Если включен режим поворота, применяем трансформацию
                    if (rotateGraph) {
                        canvas.translate(getWidth() / 2, getHeight() / 2); // Перемещаем начало координат в центр окна
                        canvas.rotate(-Math.PI / 2); // Поворачиваем график на 90 градусов влево
                        canvas.translate(-getWidth() / 2,-getHeight() / 2); // Корректируем положение
                    }

                    Stroke oldStroke = canvas.getStroke();
                    Color oldColor = canvas.getColor();
                    Paint oldPaint = canvas.getPaint();
                    Font oldFont = canvas.getFont();
// Шаг 8 - В нужном порядке вызвать методы отображения элементов графика
// Порядок вызова методов имеет значение, т.к. предыдущий рисунок будет затираться последующим
// Первыми (если нужно) отрисовываются оси координат.


                    if(showPolygons) searchPolygon(g);
                    if (showAxis) paintAxis(canvas);
// Затем отображается сам график
                    paintGraphics(canvas);
// Затем (если нужно) отображаются маркеры точек, по которым строился график.
                    if (showMarkers) paintMarkers(canvas);


// Шаг 9 - Восстановить старые настройки холста
                    canvas.setFont(oldFont);
                    canvas.setPaint(oldPaint);
                    canvas.setColor(oldColor);
                    canvas.setStroke(oldStroke);
                }



                protected void paintGraphics(Graphics2D canvas) {
                    canvas.setStroke(graphicsStroke);
                    canvas.setColor(Color.BLACK);

                    GeneralPath graphics = new GeneralPath();
                    boolean pointWasChanged = false;

                    for (int i = 0; i < graphicsData.length; i++) {
                        Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);

                        // Проверяем, выбрана ли именно эта точка (по X и Y координатам)
                        if (changeSelectedPoint && selectedPoint != null &&
                                Math.abs(point.getX() - selectedPoint.getX()) < 5) {

                            // Обновляем только координату Y для выбранной точки
                            double newY = pointToXY(new Point2D.Double(point.getX(), movedPoint.getY())).getY();
                            graphicsData[i][1] = newY;
                            point = xyToPoint(graphicsData[i][0], newY);
                            pointWasChanged = true;
                        }

                        if (i > 0) {
                            graphics.lineTo(point.getX(), point.getY());
                        } else {
                            graphics.moveTo(point.getX(), point.getY());
                        }
                    }

                    if (pointWasChanged) {
                        repaint();
                    }

                    canvas.draw(graphics);

                    // Дополнительные элементы интерфейса для масштабирования
                    if (scalePoint1 != null && scalePoint2 != null) {
                        zoom();
                        graphicsDataList.clear();
                        zoomPoint1 = null;
                        zoomPoint2 = null;
                        scalePoint1 = null;
                        scalePoint2 = null;
                    }
                    if (zoomPoint1 != null && zoomPoint2 != null) {
                        canvas.setStroke(zoomWindowStroke);
                        canvas.setColor(Color.BLUE);
                        canvas.drawRect((int) zoomPoint1.getX(), (int) zoomPoint1.getY(),
                                (int) (zoomPoint2.getX() - zoomPoint1.getX()),
                                (int) (zoomPoint2.getY() - zoomPoint1.getY()));
                    }
                }



                public static boolean areDigitsInAscendingOrder(String number) {
                    // Удаляем запятую
                    String numberStr = number.replace(".", "");

                    // Проверяем, что каждая цифра меньше следующей
                    for (int i = 0; i < numberStr.length() - 1; i++) {
                        if (numberStr.charAt(i) >= numberStr.charAt(i + 1)) {
                            return false;
                        }
                    }
                    return true;
                }
                // Отображение маркеров точек, по которым рисовался график
                protected void paintMarkers(Graphics2D canvas) {
                    // Установим специальное перо для черчения маркеров
                    canvas.setStroke(markerStroke);
                    canvas.setFont(new Font("Arial", Font.PLAIN, 12));
                    canvas.setColor(Color.BLACK);

                    // Цикл по всем точкам графика
                    for (Double[] point : graphicsData) {
                        boolean temp = areDigitsInAscendingOrder(point[0].toString());

                        // Определение размера маркера относительно масштаба
                        double scale = 0.015;  // Зададим базовый масштаб (регулируйте для более точного отображения)
                        double size = scale * Math.min(getWidth(), getHeight()); // Зависимость от размера окна
                        double halfSize = size / 2;
                        double endSize = halfSize / 2;
                        double diagonalSize = halfSize * Math.sqrt(2) / 1.4; // Половина диагонали звезды

                        // Преобразование координат точки для рисования
                        Point2D.Double center = xyToPoint(point[0], point[1]);
                        if (temp) {
                            // Если условие выполняется, рисуем звёздочку с синим цветом
                            canvas.setStroke(axisStroke);
                            canvas.setColor(Color.BLUE);
                        }
                        else {
                            // Если не выполняется, рисуем звёздочку с чёрным цветом
                            canvas.setStroke(axisStroke);
                            canvas.setColor(Color.BLACK);
                        }
                        if(selectedPoint != null &&
                                ((selectedPoint.getX() - 5) < center.getX() &&
                                        center.getX() < (selectedPoint.getX() + 5) &&
                                        (selectedPoint.getY() - 5) < center.getY() &&
                                        center.getY() < (selectedPoint.getY() + 5)))
                        {

                            canvas.setStroke(axisStroke);
                            canvas.setColor(Color.BLACK);
                            String areaText = String.format("X: %s Y: %s",point[0].floatValue(), point[1].floatValue());
                            canvas.drawString(areaText, (float) center.getX()+15, (float) center.getY()+15);
                            canvas.setColor(Color.GREEN);
                        }
                        // Рисуем звёздочку (*)
                        // Рисуем вертикальную линию
                        canvas.draw(new Line2D.Double(center.getX(), center.getY() - halfSize,
                                center.getX(), center.getY() + halfSize));
                        // Рисуем горизонтальную линию
                        canvas.draw(new Line2D.Double(center.getX() - halfSize, center.getY(),
                                center.getX() + halfSize, center.getY()));

                        // Рисуем горизонтальные концы для вертикальной линии
                        canvas.draw(new Line2D.Double(center.getX() - endSize, center.getY() - halfSize,
                                center.getX() + endSize, center.getY() - halfSize));
                        canvas.draw(new Line2D.Double(center.getX() - endSize, center.getY() + halfSize,
                                center.getX() + endSize, center.getY() + halfSize));

                        // Рисуем вертикальные концы для горизонтальной линии
                        canvas.draw(new Line2D.Double(center.getX() - halfSize, center.getY() - endSize,
                                center.getX() - halfSize, center.getY() + endSize));
                        canvas.draw(new Line2D.Double(center.getX() + halfSize, center.getY() - endSize,
                                center.getX() + halfSize, center.getY() + endSize));
                    }
                }


                // Метод, обеспечивающий отображение осей координат
                protected void paintAxis(Graphics2D canvas) {
// Установить особое начертание для осей
                    canvas.setStroke(axisStroke);
// Оси рисуются чѐрным цветом
                    canvas.setColor(Color.BLACK);
// Стрелки заливаются чѐрным цветом
                    canvas.setPaint(Color.BLACK);
// Подписи к координатным осям делаются специальным шрифтом
                    canvas.setFont(axisFont);
// Создать объект контекста отображения текста - для получения характеристик устройства (экрана)
                    FontRenderContext context = canvas.getFontRenderContext();
// Определить, должна ли быть видна ось Y на графике
                    if (minX<=0.0 && maxX>=0.0) {
// Она должна быть видна, если левая граница показываемой области (minX) <= 0.0,
                        // а правая (maxX) >= 0.0
// Сама ось - это линия между точками (0, maxY) и (0, minY)
                        canvas.draw(new Line2D.Double(xyToPoint(0, maxY),
                                xyToPoint(0, minY)));
// Стрелка оси Y
                        GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на верхний конец оси Y
                        Point2D.Double lineEnd = xyToPoint(0, maxY);
                        arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести левый "скат" стрелки в точку с относительными координатами (5,20)
                        arrow.lineTo(arrow.getCurrentPoint().getX()+5,
                                arrow.getCurrentPoint().getY()+20);
// Вести нижнюю часть стрелки в точку с относительными координатами (-10, 0)
                        arrow.lineTo(arrow.getCurrentPoint().getX()-10,
                                arrow.getCurrentPoint().getY());
// Замкнуть треугольник стрелки
                        arrow.closePath();
                        canvas.draw(arrow); // Нарисовать стрелку
                        canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси Y
// Определить, сколько места понадобится для надписи "y"
                        Rectangle2D bounds = axisFont.getStringBounds("y", context);
                        Point2D.Double labelPos = xyToPoint(0, maxY);
// Вывести надпись в точке с вычисленными координатами
                        canvas.drawString("y", (float)labelPos.getX() + 10,
                                (float)(labelPos.getY() - bounds.getY()));
                    }
// Определить, должна ли быть видна ось X на графике
                    if (minY<=0.0 && maxY>=0.0) {
// Она должна быть видна, если верхняя граница показываемой области (maxX) >= 0.0,
// а нижняя (minY) <= 0.0
                        canvas.draw(new Line2D.Double(xyToPoint(minX, 0),
                                xyToPoint(maxX, 0)));
// Стрелка оси X
                        GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на правый конец оси X
                        Point2D.Double lineEnd = xyToPoint(maxX, 0);
                        arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести верхний "скат" стрелки в точку с относительными координатами (-20,-5)
                        arrow.lineTo(arrow.getCurrentPoint().getX()-20,
                                arrow.getCurrentPoint().getY()-5);
// Вести левую часть стрелки в точку с относительными координатами (0, 10)
                        arrow.lineTo(arrow.getCurrentPoint().getX(),
                                arrow.getCurrentPoint().getY()+10);
// Замкнуть треугольник стрелки
                        arrow.closePath();
                        canvas.draw(arrow); // Нарисовать стрелку
                        canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси X
// Определить, сколько места понадобится для надписи "x"
                        Rectangle2D bounds = axisFont.getStringBounds("x", context);
                        Point2D.Double labelPos = xyToPoint(maxX, 0);
// Вывести надпись в точке с вычисленными координатами
                        canvas.drawString("x", (float)(labelPos.getX() -
                                bounds.getWidth() - 10), (float)(labelPos.getY() + bounds.getY()));
                    }
                }


                /* Метод-помощник, осуществляющий преобразование координат.
                * Оно необходимо, т.к. верхнему левому углу холста с координатами
                * (0.0, 0.0) соответствует точка графика с координатами (minX, maxY),
                где
                * minX - это самое "левое" значение X, а
                * maxY - самое "верхнее" значение Y.
                */
                protected Point2D.Double xyToPoint(double x, double y) {
// Вычисляем смещение X от самой левой точки (minX)
                    double deltaX = x - minX;
// Вычисляем смещение Y от точки верхней точки (maxY)
                    double deltaY = maxY - y;
                    return new Point2D.Double(deltaX * scale, deltaY * scale);
                }

                protected Point2D.Double pointToXY(Point2D.Double point) {
                    // Преобразуем экранные координаты обратно в координаты графика
                    double x = point.getX() / scale + minX;
                    double y = maxY - point.getY() / scale;
                    return new Point2D.Double(x, y);
                }

                /* Метод-помощник, возвращающий экземпляр класса Point2D.Double
                 * смещѐнный по отношению к исходному на deltaX, deltaY
                 * К сожалению, стандартного метода, выполняющего такую задачу, нет.
                 */
                protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX,
                                                    double deltaY) {
// Инициализировать новый экземпляр точки
                    Point2D.Double dest = new Point2D.Double();
// Задать еѐ координаты как координаты существующей точки + заданные смещения
                    dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
                    return dest;
                }


            }
