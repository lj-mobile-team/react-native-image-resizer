//
//  ImageHelpers.h
//  ImageResizer
//
//  Created by Alexander Blokhin on 10.07.2020.
//  Copyright © 2020 Facebook. All rights reserved.
//

#ifndef ImageHelpers_h
#define ImageHelpers_h

#include <UIKit/UIKit.h>

extern const CGBitmapInfo kDefaultCGBitmapInfo;
extern const CGBitmapInfo kDefaultCGBitmapInfoNoAlpha;

float GetScaleForProportionalResize(CGSize theSize, CGSize intoSize, bool onlyScaleDown, bool maximize);
CGContextRef CreateCGBitmapContextForWidthAndHeight(unsigned int width, unsigned int height, CGColorSpaceRef optionalColorSpace, CGBitmapInfo optionalInfo);

CGImageRef CreateCGImageFromUIImageScaled(UIImage* inImage, float scaleFactor);

@interface UIImage (scale)
    -(UIImage*)scaleToSize:(CGSize)toSize;
@end

#endif /* ImageHelpers_h */
