#import <UIKit/UIKit.h>
#import "ImageResizer.h"
#include "ImageHelpers.h"

@implementation ImageResizer

RCT_EXPORT_MODULE()

- (NSData *)getImageData:(NSURL *)url image:(UIImage *)image {
    BOOL isJPG = [url.pathExtension.lowercaseString  isEqual: @"jpg"] || [url.pathExtension.lowercaseString  isEqual: @"jpeg"];
    
    if (isJPG) {
        return UIImageJPEGRepresentation(image, 1.0);
    } else {
        return UIImagePNGRepresentation(image);
    }
}

RCT_EXPORT_METHOD(resizedBase64:(NSString *)imageUrl
                  width:(float)width
                  height:(float)height
                  resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    
    CGSize newSize = CGSizeMake(width, height);
    NSURL *url = [NSURL URLWithString: imageUrl];
    
    dispatch_async(dispatch_get_global_queue(0,0), ^{
        NSData * data = [[NSData alloc] initWithContentsOfURL: url];
        
        if (data == nil) {
            resolve(@{});
            return;
        }
        
        UIImage *originalImage =  [UIImage imageWithData: data];
        UIImage *scaledImage = [originalImage scaleToSize:newSize];
        
        NSData *imageData = [self getImageData:url image:scaledImage];
        NSString *base64Encoded = [imageData base64EncodedStringWithOptions:0];
        
        resolve(base64Encoded);
    });
}

RCT_EXPORT_METHOD(resizeImage:(NSString *)imageUrl
                  width:(float)width
                  height:(float)height
                  resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    
    CGSize newSize = CGSizeMake(width, height);
    NSURL *url = [NSURL URLWithString: imageUrl];
    
    dispatch_async(dispatch_get_global_queue(0,0), ^{
        NSData * data = [[NSData alloc] initWithContentsOfURL: url];
        
        if (data == nil) {
            resolve(@{});
            return;
        }
        
        UIImage *originalImage =  [UIImage imageWithData: data];
        
        if (originalImage.size.width < width && originalImage.size.height < height) {
            resolve(@{});
            return; 
        }
        
        UIImage *scaledImage = [originalImage scaleToSize:newSize];
        
        NSData *imageData = [self getImageData:url image:scaledImage];
        
        if (![imageData writeToURL:url atomically:YES]) {
            NSLog(@"Failed to save image data to disk");
        }
        
        resolve(@{});
    });
}

@end
